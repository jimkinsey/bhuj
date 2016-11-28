package bhuj.rendering

import bhuj.Mustache
import bhuj.Mustache.Templates
import bhuj.model.{Partial, Section, Template, Text}
import org.scalatest.FunSpec

class OptimiserTests extends FunSpec {
  import org.scalatest.Matchers._

  describe("Optimising a template") {

    it("leaves an empty template unchanged") {
      new Optimiser(parse).optimise(Template()) should be(Right(Template()))
    }

    it("inlines non-recursive partials") {
      val templates: Templates = {
        case "action" => Some("do")
        case _        => None
      }
      val template = Template(
        Text("Let's "), Partial("action"), Text(" this!")
      )
      new Optimiser(parse, templates).optimise(template) should be(Right(Template(Text("Let's "), Text("do"), Text(" this!"))))
    }

    it("does not inline recursive partials referenced in sections") {
      val templates: Templates = {
        case "action" => Some("{{#if-action}}{{> action}}{{/if-action}}")
        case _        => None
      }
      val template = Template(
        Text("Let's "), Partial("action"), Text(" this!")
      )
      new Optimiser(parse, templates).optimise(template) should be(Right(template))
    }

    it("does not inline recursive partials referenced in other partials") {
      val templates: Templates = {
        case "recurses-eventually" => Some("{{> recurse}}")
        case "recurse" => Some("{{#if-action}}{{> recurses-eventually}}{{/if-action}}")
        case _        => None
      }
      val template = Template(
        Text("Some text"), Partial("recurses-eventually")
      )
      new Optimiser(parse, templates).optimise(template) should be(Right(template))
    }

    it("optimises the templates of non-recursive partials") {
      val templates: Templates = {
        case "action" => Some("{{#if-action}}{{> other}}{{/if-action}}")
        case "other"  => Some("Hello!")
        case _        => None
      }
      val template = Template(Partial("action"))
      new Optimiser(parse, templates).optimise(template) should be(Right(Template(Section("if-action", Template(Text("Hello!"))))))
    }

    it("concatenates neighbouring text components") {
      pendingUntilFixed {
        new Optimiser(parse).optimise(Template(Text("foo"), Text("bar"))) should be(Right(Template(Text("foobar"))))
      }
    }

  }

  private lazy val parse = new Mustache().parse

}
