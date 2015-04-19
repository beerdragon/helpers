package uk.co.beerdragon.junit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Helpers for event waiting in unit tests. These should be used whenever a condition that indicates
 * a successful inter-thread event requires testing. Failure can typically only be assumed after a
 * timeout has elapsed without observing said condition. Using these helpers allows such a timeout
 * to be controlled centrally (for example based on the speed and resources available on the build
 * agent running the test).
 */
public final class Wait {

  private static final int MAX_WAIT = 5000;

  private Wait () {
  }

  /**
   * Wait on a {@link CountDownLatch}.
   * 
   * @param latch
   *          The latch to wait on.
   * @throws AssertionError
   *           if the wait did not complete.
   */
  public static void latch (final CountDownLatch latch) {
    try {
      assertTrue (latch.await (MAX_WAIT, TimeUnit.MILLISECONDS));
    } catch (final InterruptedException e) {
      fail ();
    }
  }

  /**
   * Wait on a {@link Future}.
   * 
   * @param future
   *          The future to wait on.
   * @throws AssertionError
   *           if the wait did not complete.
   */
  public static <T> T future (final Future<T> future) {
    try {
      return future.get (MAX_WAIT, TimeUnit.MILLISECONDS);
    } catch (final Exception e) {
      throw new AssertionError (e);
    }
  }

  /**
   * Wait on a {@link CyclicBarrier}.
   * 
   * @param barrier
   *          The barrier to wait on.
   * @throws AssertionError
   *           if the wait did not complete.
   */
  public static void barrier (final CyclicBarrier barrier) {
    try {
      barrier.await (MAX_WAIT, TimeUnit.MILLISECONDS);
    } catch (final Exception e) {
      throw new AssertionError (e);
    }
  }

  /**
   * Wait on a {@link ExecutorService}.
   * 
   * @param executor
   *          The executor to wait on.
   * @throws AssertionError
   *           if the wait did not complete.
   */
  public static void executor (final ExecutorService executor) {
    try {
      executor.awaitTermination (MAX_WAIT, TimeUnit.MILLISECONDS);
    } catch (final Exception e) {
      throw new AssertionError (e);
    }
  }

  /**
   * Wait on a {@link BlockingQueue}.
   * 
   * @param queue
   *          The queue to wait on.
   * @return The value read from the queue.
   * @throws AssertionError
   *           if the wait did not complete.
   */
  public static <T> T queue (final BlockingQueue<T> queue) {
    try {
      final T value = queue.poll (MAX_WAIT, TimeUnit.MILLISECONDS);
      assertNotNull (value);
      return value;
    } catch (final InterruptedException e) {
      throw new AssertionError (e);
    }
  }
}