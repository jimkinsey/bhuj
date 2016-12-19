Bhuj
===
An implementation of [Mustache](https://mustache.github.io/mustache.5.html) logic-less templates in Scala.

[![Build Status](https://travis-ci.org/jimkinsey/bhuj.png?branch=master)](https://travis-ci.org/jimkinsey/bhuj)

    mustache.render("Hello, {{name}}!", Person(name = "Charlotte")) // Right("Hello, Charlotte!")

Table of Contents
---

* [Installation](#installation)
* [Usage](#usage)
* [Setting Up](#setting-up)
* [Partials](#partials)
* [Results](#results)
* [Optional Values](#optional-values)
* [Lambdas](#lambdas)
* [Global Context](#global-context)
* [Contextualisation](#contextualisation)

Installation
---

Assuming the Sonatype OSS Snapshots repo is set up, the current snapshot is available to SBT like so:

    com.github.jimkinsey" %% "bhuj" % "0.1-SNAPSHOT

Usage
---

Assuming a template `greeting.mustache` with content `Hello {{name}}!` is in the `templates` dir:

    import ContextImplicits._
    import MustacheBuilder._

    val mustache = mustacheRenderer.withTemplatePath("templates")
    case class Person(name: String)
    mustache.renderTemplate("greeting", Person(name = "Charlotte")) map (_.right.get)

Results in a Future containing `Hello Charlotte!`.

## Setting Up

The Mustache renderer may either be constructed directly or set up using a provided builder (this is the recommended way):

    import MustacheBuilder._

    val mustache = mustacheRenderer.withTemplatePath("templates").withCache

The builder is _immutable_ meaning it can be safely reused and used as the basis of further building:

    val mustacheWithTemplates = mustacheRenderer.withTemplatePath("templates")
    val cachedMustache = mustacheWithTemplates.withCache
    val uncachedMustache = mustacheWithTemplates.withoutCache

The following builder methods are provided:

* `withTemplatePath(path: String)` sets up with a `FilePartialLoader` with the provided path.
* `withTemplates(templates: (String,String)*)` converts the Tuples to a Map of partials, providing cleaner syntax.
* `withCacheEnabled(enabled: Boolean)` / `withCache` / `withoutCache` sets whether the provided partial source is cached.
* `withGlobalValues(pairs: (String,Any)*)` provides convenient syntax for specifying global context items
* `withHelpers(helpers: (String,Lambda)*)` is a special case of `withGlobalValues` which specifically takes Lambdas for tasks like internationalisation.

## Partials

Bhuj provides a `renderTemplate` method which takes the name of a template in place of a template string. The renderer needs to be set up with the named partial in advance, which in its rawest form is a function of template name to future of option of template:

    String => Future[Option[String]]

This allows flexibility of template source - it could be an in-memory Map, or a function to load from the file system or a remote URL or a database for example.

    val templates = {
      case "person"  => Future.successful(Some("Name: {{name}}, Address: {{#address}}{{> address}}{{/address}}"))
      case "address" => Future.successful(Some("{{number}} {{streetName}}, {{city}} {{postcode}}"))
      case _         => Future.successful(None)
    }
    val mustache = new Mustache(templates = templates)
    val person = Person("John Watson", Address(221, "Baker Street", "London", "NW16XE"))
    mustache.renderTemplate("person", person) // "Name: John Watson, Address: 221 Baker Street, London NW16XE"

Bhuj provides a way of loading partials from the file system:

    val fileTemplates = new FilePartialLoader("resources/templates").partial
    val mustache = new Mustache(templates = fileTemplates)

The mustache renderer may be set up to cache partials:

    mustacheRenderer.withCache

This means that the partial loading function will not be invoked with the same name twice. When working with partial sources using IO it is recommended to enable this in production but disable in development to allow rapid turnaround of template changes.

## Results

Bhuj to encapsulate the result of rendering in a future of either, following the convention of failure on the left and success on the right.

    Future[Either[Failure, String]]

Failure is a sealed trait and may be any of the following types:

* `UnclosedTag` An opening section tag for which no closing tag could be found.
* `InvalidDelimiters` invalid delimiters specified when using the Set Delimiters tag. See the [spec](https://mustache.github.io/mustache.5.html#Set-Delimiter).
* `ContextualisationFailure` It was not possible to turn the provided context object into a Map for rendering. Wraps the failure from the contextualisation process.
* `TemplateNotFound` No template was found with the specified name.
* `LambdaFailure` A failure occurred while invoking a Lambda function when rendering. Wraps the key of the Lambda function and the failure it returned.

## Optional values

Optional values are not part of the official Mustache spec as it is language-agnostic. Bhuj has two ways of dealing with optional values when used as the context value for a section tag.

If the value is defined and is a Context (i.e. Map) then the enclosed template is rendered in that context:

    val person = Person(address = Some(Address(postcode = "NW16XE"))
    mustache.render("{{#address}}{{postcode}}{{/address}}", person) // "NW16XE"

If the value is defined and _not_ a context it will be passed to the enclosed template as underscore (`_`):

    val address = Address(221, Some("B"))
    mustache.render("{{number}}{{#letter}}{{_}}{{/letter}}") // "221B"

If the value is not defined then the enclosed template will not be rendered.

    val address = Address(221, None)
    mustache.render("No: {{number}}{{#letter}}, Letter: {{_}}{{/letter}}") // "No: 221"

## Lambdas

The mustache spec for sections includes support for "Lambdas", functions which are applied to the content of the section.

In Bhuj the signature for a Lambda is like so:

    (String, (String) => Future[Result])) => Future[Either[Any, String]]

Note that the practice of using an Either where Right contains the successful result and Left a failure is applied here.

The first argument of type `String` is the content of a section, the second argument is a function which will render a Mustache template in the current context. `Result` is a type alias for `Either[Failure, String]`.

    val shout: Lambda = (text, rendered) => Future.successful(Right(text.toUpperCase))
    mustache.render("{{#shout}}I'm on the train{{/shout}}", Map("shout" -> shout)) // "I'M ON THE TRAIN"


Global Context
---

Values which should be available in the context at all levels may be placed in a global context for the renderer:

    mustacheRenderer
      .withGlobalValues("n" -> 42)
      .render("{{n}} {{#displayN}}{{n}}{{/displayN}}", Map("displayN" -> true)) // "42 42"

The global context is also a convenient place to put lambdas for tasks like localisation.

Contextualisation
---

A hook is provided by which any object may be converted to a mustache context for rendering, using typeclasses.

Some basic typeclasses are provided which appropriately transform maps and case classes, and can be imported into scope like so:

    import ContextImplicits._

It is relatively straightforward to provide your own by implementing `CanContextualise[T]` which takes type `T` and converts it to a `Map[String, Any]` and placing it implicitly in scope:

    implicit object CanContextualiseInt extends CanContextualise[Int] {
      def context(int: Int) = Right(Map("value" -> int))
    }

    mustache.render("The answer is {{value}}", 42) // "The answer is 42"

