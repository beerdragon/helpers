package uk.co.beerdragon.junit;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

import com.google.common.base.Supplier;

/**
 * Rule for a pool of threads that will be terminated at the end of a test. For example:
 * 
 * <pre class="java">
 * <span class="k">public class</span> <span class="i">UsesChildThreads</span> {
 *
 *  <span class="i">&#064;Rule</span>
 *  <span class="k">public</span> <span class="i">Threads threads</span> = <span class="k">new</span> <span class="i">Threads</span> ();
 *
 *  <span class="i">&#064;Test</span>
 *  <span class="k">public void</span> <span class="i">testUsingChildThread</span> () {
 *    <span class="i">threads</span>.<span class="i">start</span> (<span class="k">new</span> <span class="i">Runnable</span> () {
 *      <span class="c">// ...</span>
 *    });
 *    <span class="i">threads</span>.<span class="i">start</span> (<span class="k">new</span> <span class="i">Runnable</span> () {
 *      <span class="c">// ...</span>
 *    });
 *  }
 * }
 * </pre>
 * 
 * Any exceptions thrown by spawned tasks will cause test failure.
 */
public class Threads implements TestRule {

  /**
   * Thread local storage of the underlying executor. The thread running a test method will create
   * an executor before calling the method and destroy it afterwards. Any threads created by the
   * executor will have the executor set in their local storage so that they can launch tasks if
   * needed. This pattern ensures spawned threads always observe the correct executor (although it
   * will reject further submissions) if they outlive the call original test method.
   */
  private final ThreadLocal<ExecutorService> _executor = new ThreadLocal<ExecutorService> ();

  private final Logger _logger;

  /**
   * Creates a new instance.
   */
  public Threads () {
    this (Logger.getLogger (Threads.class.getName ()));
  }

  /**
   * Creates a new instance.
   * 
   * @param logger
   *          The logging instance to write to, not {@code null}.
   */
  public Threads (final Logger logger) {
    _logger = logger;
  }

  /**
   * Wraps the runnable in a form that will translate the stack frames of any exceptions so that
   * they include the history of calls that requested the task be launched rather than start at a
   * thread created by the executor.
   * 
   * @param task
   *          The runnable to wrap, never {@code null}.
   * @return The wrapped runnable, never {@code null}.
   */
  private Runnable fixStackTrace (final Runnable task) {
    final StackTraceElement[] startStack = Thread.currentThread ().getStackTrace ();
    return new Runnable () {

      @Override
      public void run () {
        try {
          _logger.info ("Running: " + task);
          task.run ();
          _logger.info ("Finished: " + task);
        } catch (RuntimeException | Error e) {
          final StackTraceElement[] taskStack = e.getStackTrace ();
          int task = 0;
          while (task < taskStack.length
              && !taskStack[task].getClassName ().startsWith (getClass ().getName ())) {
            task++;
          }
          if (task < taskStack.length) task++;
          int start = 0;
          while (start < startStack.length
              && !Threads.class.getName ().equals (startStack[start].getClassName ())) {
            start++;
          }
          if (start < startStack.length) start++;
          final StackTraceElement[] stack = new StackTraceElement[task + startStack.length - start];
          System.arraycopy (taskStack, 0, stack, 0, task);
          System.arraycopy (startStack, start, stack, task, startStack.length - start);
          e.setStackTrace (stack);
          _logger.log (Level.WARNING, "Task failed", e);
          throw e;
        }
      }
    };
  }

  /**
   * Submits a task to the executor service managed by this rule.
   * 
   * @param task
   *          The task to run.
   */
  public void start (final Runnable task) {
    final ExecutorService executor = _executor.get ();
    assert executor != null;
    _logger.fine ("Spawning task: " + task.toString ());
    executor.execute (fixStackTrace (task));
  }

  /**
   * Creates threads for the executor. The thread local {@link #_executor} is configured for each in
   * order to support calls to {@link #start} from the spawned threads correctly.
   */
  private class ThreadFactoryImpl implements ThreadFactory {

    private final ThreadFactory _default = Executors.defaultThreadFactory ();

    private final AtomicInteger _count = new AtomicInteger ();

    private final Supplier<ExecutorService> _service;

    private final String _testName;

    private final Collection<Throwable> _errors;

    public ThreadFactoryImpl (final Supplier<ExecutorService> service, final String testName,
        final Collection<Throwable> errors) {
      _service = service;
      _testName = testName;
      _errors = errors;
    }

    @Override
    public Thread newThread (final Runnable r) {
      final Thread thread = _default.newThread (new Runnable () {

        @Override
        public void run () {
          assert _executor.get () == null;
          _executor.set (_service.get ());
          try {
            r.run ();
          } catch (final Throwable t) {
            _errors.add (t);
          } finally {
            _executor.set (null);
          }
        }
      });
      thread.setName (_testName + "-" + _count.incrementAndGet ());
      return thread;
    }
  }

  private class Executor {

    private final ExecutorService _service;

    public Executor (final String testName, final Collection<Throwable> errors) {
      _service = Executors.newCachedThreadPool (new ThreadFactoryImpl (
          new Supplier<ExecutorService> () {

            @Override
            public ExecutorService get () {
              return _service;
            }
          }, testName, errors));
    }

  }

  private void setup (final Description description, final Collection<Throwable> errors) {
    assert _executor.get () == null;
    _executor.set (new Executor (description.getDisplayName (), errors)._service);
  }

  private void shutdown () {
    final ExecutorService executor = _executor.get ();
    assert executor != null;
    _executor.set (null);
    _logger.finest ("Shutting down spawned threads");
    executor.shutdown ();
    _logger.finest ("Waiting for threads to terminate");
    Wait.executor (executor);
    _logger.finest ("Threads terminated");
  }

  // TestRule

  @Override
  public Statement apply (final Statement stmt, final Description description) {
    return new Statement () {

      @Override
      public void evaluate () throws Throwable {
        final List<Throwable> errors = Collections.synchronizedList (new LinkedList<Throwable> ());
        _logger.fine ("Test started");
        try {
          setup (description, errors);
          try {
            stmt.evaluate ();
          } finally {
            shutdown ();
            _logger.finest ("Checking for exceptions");
          }
        } finally {
          _logger.fine ("Test complete");
          MultipleFailureException.assertEmpty (errors);
        }
      }
    };
  }

}