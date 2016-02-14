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
    mustache.render("greeting", Person(name = "Charlotte")).right.get

Results in `Hello Charlotte!`.

Roadmap
---
* support set delimiter tag
* improve README - more examples
* Mustache should return Mustache.Failure, NOT Any
* Tidy up the template parser
