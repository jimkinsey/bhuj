package bhuj.rendering

import bhuj.Mustache
import bhuj.Mustache.Templates
import bhuj.model.{Partial, Section, Template, Text}
import org.scalatest.AsyncFunSpec

import scala.concurrent.Future

class OptimiserTests extends AsyncFunSpec {
  import org.scalatest.Matchers._

  describe("Optimising a template") {

    it("leaves an empty template unchanged") {
      new Optimiser(parse).optimise(Template()) map (_ shouldBe Right(Template()))
    }

    it("inlines non-recursive partials") {
      val templates: Templates = {
        case "action" => Future.successful(Some("do"))
        case _        => Future.successful(None)
      }
      val template = Template(
        Text("Let's "), Partial("action"), Text(" this!")
      )
      new Optimiser(parse, templates).optimise(template) map(_ shouldBe Right(Template(Text("Let's "), Text("do"), Text(" this!"))))
    }

    it("does not inline recursive partials referenced in sections") {
      val templates: Templates = {
        case "action" => Future.successful(Some("{{#if-action}}{{> action}}{{/if-action}}"))
        case _        => Future.successful(None)
      }
      val template = Template(
        Text("Let's "), Partial("action"), Text(" this!")
      )
      new Optimiser(parse, templates).optimise(template) map (_ shouldBe Right(template))
    }

    it("does not inline recursive partials referenced in other partials") {
      val templates: Templates = {
        case "recurses-eventually" => Future.successful(Some("{{> recurse}}"))
        case "recurse"             => Future.successful(Some("{{#if-action}}{{> recurses-eventually}}{{/if-action}}"))
        case _                     => Future.successful(None)
      }
      val template = Template(
        Text("Some text"), Partial("recurses-eventually")
      )
      new Optimiser(parse, templates).optimise(template) map (_ shouldBe Right(template))
    }

    it("optimises the templates of non-recursive partials") {
      val templates: Templates = {
        case "action" => Future.successful(Some("{{#if-action}}{{> other}}{{/if-action}}"))
        case "other"  => Future.successful(Some("Hello!"))
        case _        => Future.successful(None)
      }
      val template = Template(Partial("action"))
      new Optimiser(parse, templates).optimise(template) map (_ shouldBe Right(Template(Section("if-action", Template(Text("Hello!"))))))
    }

  }

  private lazy val parse = new Mustache().parse

}
