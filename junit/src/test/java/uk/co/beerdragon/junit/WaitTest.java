/*
 * JUnit testing utilities.
 *
 * Copyright 2015 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.junit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

/**
 * Tests {@link Wait}.
 */
public class WaitTest {

  /**
   * Tests latch completion.
   */
  @Test
  public void testLatchCompletion () throws InterruptedException {
    final CountDownLatch latch = mock (CountDownLatch.class);
    when (latch.await (Wait.maxWait (), TimeUnit.MILLISECONDS)).thenReturn (true);

    Wait.latch (latch);
  }

  /**
   * Tests latch timeout.
   */
  @Test (expected = AssertionError.class)
  public void testLatchTimeout () throws InterruptedException {
    final CountDownLatch latch = mock (CountDownLatch.class);
    when (latch.await (Wait.maxWait (), TimeUnit.MILLISECONDS)).thenReturn (false);

    Wait.latch (latch);
  }

  /**
   * Tests latch interruption.
   */
  @Test (expected = AssertionError.class)
  public void testLatchInterrupt () throws InterruptedException {
    final CountDownLatch latch = mock (CountDownLatch.class);
    when (latch.await (Wait.maxWait (), TimeUnit.MILLISECONDS)).thenThrow (
        new InterruptedException ());

    Wait.latch (latch);
  }

  /**
   * Tests future completion.
   */
  @SuppressWarnings ("unchecked")
  @Test
  public void testFutureCompletion () throws Throwable {
    @SuppressWarnings ("rawtypes")
    final Future future = mock (Future.class);
    when (future.get (Wait.maxWait (), TimeUnit.MILLISECONDS)).thenReturn ("FOO");

    assertEquals ("FOO", Wait.future (future));
  }

  /**
   * Tests future timeout.
   */
  @SuppressWarnings ("unchecked")
  @Test (expected = AssertionError.class)
  public void testFutureTimeout () throws Throwable {
    @SuppressWarnings ("rawtypes")
    final Future future = mock (Future.class);
    when (future.get (Wait.maxWait (), TimeUnit.MILLISECONDS)).thenThrow (new TimeoutException ());

    Wait.future (future);
  }

  /**
   * Tests future interruption.
   */
  @SuppressWarnings ("unchecked")
  @Test (expected = AssertionError.class)
  public void testFutureInterrupt () throws Throwable {
    @SuppressWarnings ("rawtypes")
    final Future future = mock (Future.class);
    when (future.get (Wait.maxWait (), TimeUnit.MILLISECONDS)).thenThrow (
        new InterruptedException ());

    Wait.future (future);
  }

  /**
   * Tests barrier completion.
   */
  @Test
  public void testBarrierCompletion () throws Throwable {
    final CyclicBarrier barrier = mock (CyclicBarrier.class);

    Wait.barrier (barrier);
  }

  /**
   * Tests barrier timeout.
   */
  @Test (expected = AssertionError.class)
  public void testBarrierTimeout () throws Throwable {
    final CyclicBarrier barrier = mock (CyclicBarrier.class);
    doThrow (new TimeoutException ()).when (barrier).await (Wait.maxWait (), TimeUnit.MILLISECONDS);

    Wait.barrier (barrier);
  }

  /**
   * Tests barrier interruption.
   */
  @Test (expected = AssertionError.class)
  public void testBarrierInterrupt () throws Throwable {
    final CyclicBarrier barrier = mock (CyclicBarrier.class);
    doThrow (new InterruptedException ()).when (barrier).await (Wait.maxWait (),
        TimeUnit.MILLISECONDS);

    Wait.barrier (barrier);
  }

  /**
   * Tests executor completion.
   */
  @Test
  public void testExecutorCompletion () throws Throwable {
    final ExecutorService executor = mock (ExecutorService.class);
    when (executor.awaitTermination (Wait.maxWait (), TimeUnit.MILLISECONDS)).thenReturn (true);

    Wait.executor (executor);
  }

  /**
   * Tests executor timeout.
   */
  @Test (expected = AssertionError.class)
  public void testExecutorTimeout () throws Throwable {
    final ExecutorService executor = mock (ExecutorService.class);
    when (executor.awaitTermination (Wait.maxWait (), TimeUnit.MILLISECONDS)).thenReturn (false);

    Wait.executor (executor);
  }

  /**
   * Tests executor interruption.
   */
  @Test (expected = AssertionError.class)
  public void testExecutorInterrupt () throws Throwable {
    final ExecutorService executor = mock (ExecutorService.class);
    doThrow (new InterruptedException ()).when (executor).awaitTermination (Wait.maxWait (),
        TimeUnit.MILLISECONDS);

    Wait.executor (executor);
  }

  /**
   * Tests queued value.
   */
  @SuppressWarnings ("unchecked")
  @Test
  public void testQueuedValue () throws InterruptedException {
    @SuppressWarnings ("rawtypes")
    final BlockingQueue queue = mock (BlockingQueue.class);
    when (queue.poll (Wait.maxWait (), TimeUnit.MILLISECONDS)).thenReturn ("FOO");

    assertEquals ("FOO", Wait.queue (queue));
  }

  /**
   * Tests queue timeout.
   */
  @SuppressWarnings ("unchecked")
  @Test (expected = AssertionError.class)
  public void testQueueTimeout () throws InterruptedException {
    @SuppressWarnings ("rawtypes")
    final BlockingQueue queue = mock (BlockingQueue.class);
    when (queue.poll (Wait.maxWait (), TimeUnit.MILLISECONDS)).thenReturn (null);

    Wait.queue (queue);
  }

  /**
   * Tests queue interruption.
   */
  @SuppressWarnings ("unchecked")
  @Test (expected = AssertionError.class)
  public void testQueueInterrupt () throws InterruptedException {
    @SuppressWarnings ("rawtypes")
    final BlockingQueue queue = mock (BlockingQueue.class);
    when (queue.poll (Wait.maxWait (), TimeUnit.MILLISECONDS)).thenThrow (
        new InterruptedException ());

    Wait.queue (queue);
  }

}