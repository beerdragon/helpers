/*
 * JUnit testing utilities.
 *
 * Copyright 2015 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Tests {@link ExpectException}.
 */
public class ExpectExceptionTest {

  /**
   * Tests {@link ExpectException#any}.
   */
  @Test
  public void testAny () {
    final ExpectException expect = ExpectException.any (IllegalArgumentException.class);

    assertEquals ("Expected exception that is an instance of java.lang.IllegalArgumentException",
        expect.toString ());
  }

  /**
   * Tests {@link ExpectException#that}.
   */
  @Test
  public void testThat () {
    @SuppressWarnings ("unchecked")
    final Matcher<Throwable> matcher = mock (Matcher.class);
    final ExpectException expect = ExpectException.that (matcher);
    doAnswer (new Answer<Void> () {

      @Override
      public Void answer (final InvocationOnMock invocation) throws Throwable {
        final Description description = (Description)invocation.getArguments ()[0];
        description.appendText ("STRING FROM THE MATCHER");
        return null;
      }
    }).when (matcher).describeTo (any (Description.class));

    assertEquals ("Expected exception that STRING FROM THE MATCHER", expect.toString ());
  }

  /**
   * Tests an execution that throws a different exception to the one expected.
   */
  @Test
  public void testUnexpectedException () {
    @SuppressWarnings ("unchecked")
    final Matcher<Throwable> matcher = mock (Matcher.class);
    final ExpectException expect = ExpectException.that (matcher);
    final RuntimeException t = new IllegalArgumentException ();
    when (matcher.matches (t)).thenReturn (false);

    try {
      expect.from (new Runnable () {

        @Override
        public void run () {
          throw t;
        }
      });
      fail ();
    } catch (final AssertionError e) {
      assertEquals (expect.toString (), e.getMessage ());
      assertSame (t, e.getCause ());
    }
  }

  /**
   * Tests an execution that throws the expected exception.
   */
  @Test
  public void testExpectedException () {
    @SuppressWarnings ("unchecked")
    final Matcher<Throwable> matcher = mock (Matcher.class);
    final ExpectException expect = ExpectException.that (matcher);
    final RuntimeException t = new IllegalArgumentException ();
    when (matcher.matches (t)).thenReturn (true);

    expect.from (new Runnable () {

      @Override
      public void run () {
        throw t;
      }
    });
  }

  /**
   * Tests an execution that does not throw an exception.
   */
  @Test
  public void testNoException () {
    @SuppressWarnings ("unchecked")
    final Matcher<Throwable> matcher = mock (Matcher.class);
    final ExpectException expect = ExpectException.that (matcher);

    try {
      expect.from (new Runnable () {

        @Override
        public void run () {
        }
      });
      fail ();
    } catch (final AssertionError e) {
      assertEquals (expect.toString (), e.getMessage ());
      assertNull (e.getCause ());
    }
  }
}