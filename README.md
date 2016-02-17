Scala Mustache
===
An implementation of [Mustache](https://mustache.github.io/mustache.5.html) logic-less templates in Scala.

Usage
---

Assuming a template `greeting.mustache` with content `Hello {{name}}!` is in the `templates` dir:

    import ContextImplicits._
    import MustacheBuilder._

    val mustache = mustacheRenderer.withTemplatePath("templates")
    case class Person(name: String)
    mustache.renderTemplate("greeting", Person(name = "Charlotte")).right.get

Results in `Hello Charlotte!`.

Caching
---

The mustache renderer may be set up to cache templates:

    mustacheRenderer.withCache

Global Context
---

Values which should be available in the context at all levels may be placed in a global context for the renderer:

    mustacheRenderer
      .withGlobalValues("n" -> 42)
      .render("{{n}} {{#displayN}}{{n}}{{/displayN}}", Map("displayN" -> true))

The global context is also a convenient place to put lambdas for tasks like localisation:

    mustacheRenderer
      .withHelpers("loc" -> loc)
      .render("{{#loc}}Some UI text{{/loc}}")

Contextualisation
---

A hook is provided by which any object may be converted to a mustache context for rendering, using typeclasses.

Some basic typeclasses are provided which appropriately transform maps and case classes, and can be imported into scope like so:

    import ContextImplicits._

It is relatively straightforward to provide your own by implementing `CanContextualise[T]` which takes type `T` and converts it to a `Map[String, Any]` and placing it implicitly in scope:

    implicit object CanContextualiseInt extends CanContextualise[Int] {
      def context(int: Int) = Right(Map("value" -> int))
    }

    mustache.render("The answer is {{value}}", 42)

Roadmap
---
* Mustache should return Mustache.Failure, NOT Any
* Parse failure feedback should be better - include index of failure
* ScalaCheck tests?
* build script + fail on warning