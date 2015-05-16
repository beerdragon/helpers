/*
 * JUnit testing utilities.
 *
 * Copyright 2015 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.junit;

import java.util.Objects;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

/**
 * Helpers for making assertions about code that throws exceptions.
 * <p>
 * For example:
 * 
 * <pre class="code java">
 * <span class="i">@Test</span>
 * <span class="k">public void</span> <span class="i">test</span> () {
 *   <span class="c">// ... something to set up my test</span>
 *   <span class="i">ExpectException</span>.<span class="i">any</span> (<span class="i">FileNotFoundException</span>.<span class="k">class</span>).<span class="i">from</span> (<span class="k">new</span> <span class="i">Runnable</span> () {
 *     <span class="i">@Override</span>
 *     <span class="k">public void</span> <span class="i">run</span> () {
 *       <span class="c">// ... my statement that I expect a FileNotFoundException from</span>
 *     }
 *   });
 *   <span class="c">// ... something else I want to assert</span>
 * }
 * </pre>
 */
public final class ExpectException {

  private final Matcher<Throwable> _matcher;

  private ExpectException (final Matcher<Throwable> matcher) {
    _matcher = matcher;
  }

  /**
   * Creates a new instance - a runner for items that are expected to throw an instance of the given
   * exception to be considered correct.
   * 
   * @param exception
   *          The exception type expected, not {@code null}.
   */
  @SuppressWarnings ({ "unchecked", "rawtypes" })
  public static final ExpectException any (final Class<? extends Throwable> exception) {
    return new ExpectException ((Matcher)CoreMatchers.is (exception));
  }

  /**
   * Creates a new instance - a runner for items that are expected to throw an exception that
   * satisfies the given matcher.
   * 
   * @param exception
   *          The exception matcher, not {@code null}.
   */
  public static final ExpectException that (final Matcher<Throwable> exception) {
    return new ExpectException (Objects.requireNonNull (exception));
  }

  /**
   * Executes a code fragment. The test will be failed, with an {@link AssertionError}, if the
   * expected exception is not thrown.
   * 
   * @param code
   *          The code to execute, not {@code null}.
   */
  public void from (final Runnable code) {
    try {
      code.run ();
    } catch (final Throwable t) {
      if (_matcher.matches (t)) {
        // Expected outcome
        return;
      } else {
        // Assertion failure - wrong exception thrown
        throw new AssertionError (toString (), t);
      }
    }
    // Assertion failure - no exception thrown
    throw new AssertionError (toString ());
  }

  // Object

  @Override
  public String toString () {
    final Description description = new StringDescription ();
    _matcher.describeTo (description);
    return "Expected exception that " + description.toString ();
  }

}