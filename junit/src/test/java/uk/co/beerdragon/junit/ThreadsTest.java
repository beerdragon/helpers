/*
 * JUnit testing utilities.
 *
 * Copyright 2015 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CyclicBarrier;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Tests {@link Threads}.
 */
public class ThreadsTest {

  /**
   * Tests without launching any threads.
   */
  @Test
  public void testNoThreads () throws Throwable {
    final Threads threads = new Threads ();
    final Statement statement = mock (Statement.class);
    final Description description = mock (Description.class);

    threads.apply (statement, description).evaluate ();

    verify (statement).evaluate ();
  }

  /**
   * Tests launching a slave thread.
   */
  @Test
  public void testSlaveThread () throws Throwable {
    final Threads threads = new Threads ();
    final Description description = mock (Description.class);
    final Runnable slave = mock (Runnable.class);
    final Statement statement = new Statement () {

      @Override
      public void evaluate () throws Throwable {
        threads.start (slave);
      }

    };

    threads.apply (statement, description).evaluate ();

    verify (slave).run ();
  }

  /**
   * Tests routing an exception from a slave thread. The exception is thrown to the test runner to
   * cause a failure. The stack trace is rewritten in order to show both the point where the
   * exception was thrown from and where the slave thread was started.
   */
  @Test
  public void testSlaveThreadException () throws Throwable {
    final Threads threads = new Threads ();
    final Description description = mock (Description.class);
    final Runnable slave = new Runnable () {

      @Override
      public void run () {
        throw new RuntimeException ("FOO");
      }

    };
    final Statement statement = new Statement () {

      @Override
      public void evaluate () throws Throwable {
        threads.start (slave);
      }

    };

    try {
      threads.apply (statement, description).evaluate ();
      fail ();
    } catch (final RuntimeException e) {
      assertEquals ("FOO", e.getMessage ());
      final StackTraceElement[] stack = e.getStackTrace ();
      assertEquals (slave.getClass ().getName (), stack[0].getClassName ()); // Throw point
      assertEquals (Threads.class.getName (), stack[2].getClassName ()); // Internals
      assertEquals (statement.getClass ().getName (), stack[3].getClassName ()); // Spawn point
    }
  }

  /**
   * Verifies a slave spawned slave thread is handled by the parent executor. This can be verified
   * by throwing an exception from the slave spawned thread.
   */
  @Test
  public void testSlaveSpawnedThread () throws Throwable {
    final Threads threads = new Threads ();
    final Description description = mock (Description.class);
    final Runnable slave2 = new Runnable () {

      @Override
      public void run () {
        throw new RuntimeException ("FOO");
      }

    };
    final CyclicBarrier barrier = new CyclicBarrier (2);
    final Runnable slave1 = new Runnable () {

      @Override
      public void run () {
        threads.start (slave2);
        Wait.barrier (barrier); // join main thread
      }

    };
    final Statement statement = new Statement () {

      @Override
      public void evaluate () throws Throwable {
        threads.start (slave1);
        Wait.barrier (barrier); // join slave1
      }

    };

    try {
      threads.apply (statement, description).evaluate ();
      fail ();
    } catch (final RuntimeException e) {
      assertEquals ("FOO", e.getMessage ());
    }
  }
}