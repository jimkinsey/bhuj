Scala Mustache
===
An implementation of [Mustache](https://mustache.github.io/mustache.5.html) logic-less templates in Scala.

Usage
---

Assuming a template `greeting.mustache` with content `Hello {{name}}!` is in the `templates` dir:

    import ContextImplicits._
    val templates = new FilePartialLoader("templates")
    val mustache = new Mustache(templates.partial)
    case class Person(name: String)
    mustache.render("greeting", Person(name = "Charlotte")).right.get

Results in `Hello Charlotte!`.

Roadmap
---
* support set delimiter tag
* strict mode i.e. require keys to be present and usable in the context of the tag
* improve README - more examples
* factory to build common mustache renderer case? i.e. with file templates and global context?