Miscellaneous helper scripts and resources used to build the other projects.
The main components of the project are settings for the Eclipse IDE and
resources for creating Javadoc.

Eclipse settings
----------------

All Java source code in these projects has been autoformatted using an Eclipse
plugin. The settings in `eclipse-java-formatting.xml` can be imported and, for
example, used on save to maintain code consistency.

Javadoc stylesheet
------------------

Including Java code fragments in Javadoc comments is a useful way to explain
and exemplify the classes and methods being documented. The
`javadoc-stylesheet.css` resource is a default JDK 1.7 stylesheet with some
additions to assist in formatting such code samples in a manner similar to
many popular IDEs.

A `pre` tag is used to contain code. This allows for appropriate indentation
to be used. Additional `span` tags are used to specify keywords (`k`),
identifiers (`i`), comments (`c`) and literals (`l`). For example:

~~~{.java}
/**
 * Lorum ipsem, etc, foo:
 *
 * <pre class="code java">
 *   <span class="k">public class</span> <span class="i">Foo</span> {
 *
 *     <span class="k">private int</span> <span class="i">_bar</span> = <span class="i">1234</span>;
 *
 *     <span class="c">// ... etc</span>
 *
 *   }
 * </pre>
 */
public something () {
  // ...
}
~~~

Would render with appropriate syntax highlighting defined by the stylesheet.
The styles are divided to give a generic `code` class which defines some
common formatting and then a more specific `java` class. This pattern then
allows code examples to be given in alternative languages such as C++.

