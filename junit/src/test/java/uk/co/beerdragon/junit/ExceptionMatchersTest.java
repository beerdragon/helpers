/*
 * JUnit testing utilities.
 *
 * Copyright 2015 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;

/**
 * Tests {@link ExceptionMatchers}.
 */
public class ExceptionMatchersTest {

  /**
   * Tests {@link ExceptionMatchers#hasTypeAndMessage}.
   */
  @Test
  public void testHasTypeAndMessage () {
    final Matcher<Throwable> matcher1 = ExceptionMatchers.hasTypeAndMessage (
        IllegalArgumentException.class, "Foo");
    final Matcher<Throwable> matcher2 = ExceptionMatchers.hasTypeAndMessage (
        IllegalArgumentException.class, null);

    assertFalse (matcher1.matches (null));
    assertTrue (matcher1.matches (new IllegalArgumentException ("Foo")));
    assertFalse (matcher1.matches (new IllegalArgumentException ("Bar")));
    assertFalse (matcher1.matches (new NullPointerException ("Foo")));
    assertEquals ("is java.lang.IllegalArgumentException with message 'Foo'", describe (matcher1));

    assertFalse (matcher2.matches (null));
    assertFalse (matcher2.matches (new IllegalArgumentException ("Foo")));
    assertTrue (matcher2.matches (new IllegalArgumentException ()));
    assertFalse (matcher2.matches (new NullPointerException ()));
    assertEquals ("is java.lang.IllegalArgumentException with no message", describe (matcher2));
  }

  private String describe (final Matcher<?> matcher) {
    final Description description = new StringDescription ();
    matcher.describeTo (description);
    return description.toString ();
  }
}