/*
 * JUnit testing utilities.
 *
 * Copyright 2015 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.junit;

import java.util.Objects;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Helpers for matching exceptions.
 */
public class ExceptionMatchers {

  /**
   * Prevents instantiation.
   */
  private ExceptionMatchers () {
  }

  /**
   * Matches an exception with a given type and message.
   * 
   * @param type
   *          The exception type, not {@code null}.
   * @param message
   *          The message.
   * @return The matcher, never {@code null}.
   */
  public static Matcher<Throwable> hasTypeAndMessage (final Class<? extends Throwable> type,
      final String message) {
    return new BaseMatcher<Throwable> () {

      @Override
      public boolean matches (final Object value) {
        if (value == null) {
          return false;
        } else {
          if (type.isAssignableFrom (value.getClass ())) {
            return Objects.equals (message, ((Throwable)value).getMessage ());
          } else {
            return false;
          }
        }
      }

      @Override
      public void describeTo (final Description describer) {
        describer.appendText ("is ").appendText (type.getName ());
        if (message == null) {
          describer.appendText (" with no message");
        } else {
          describer.appendText (" with message '").appendText (message).appendText ("'");
        }
      }

    };
  }
}