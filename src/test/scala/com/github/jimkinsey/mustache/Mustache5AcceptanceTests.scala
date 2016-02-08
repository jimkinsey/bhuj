package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.components.Section.Lambda
import com.github.jimkinsey.mustache.context.ContextImplicits
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class Mustache5AcceptanceTests extends FunSpec {
  import ContextImplicits._
  import MustacheBuilder.mustacheRenderer

  describe("Mustache") {

    describe("a variable tag") {

      it("is replaced by an empty string when the key is not in the context") {
        mustacheRenderer.render("Hello {{name}}", Map[String,Any]()) should be(Right("Hello "))
      }

      it("is replaced by the value from the context when present") {
        mustacheRenderer.render("Hello {{name}}", Map("name" -> "Chris")) should be(Right("Hello Chris"))
      }

      it("escapes for HTML by default") {
        mustacheRenderer.render("{{html}}", Map("html" -> """<blink>"&'</blink>""")) should be(Right("&lt;blink&gt;&quot;&amp;&#39;&lt;/blink&gt;"))
      }

      it("does not escape when the variable is triple-delimited") {
        mustacheRenderer.render("{{{html}}}", Map("html" -> """<blink>"&'</blink>""")) should be(Right("""<blink>"&'</blink>"""))
      }

    }

    describe("a section tag") {

      it("does not render when the key is not in the context") {
        mustacheRenderer.render("before:{{#x}}X{{/x}}:after") should be(Right("before::after"))
      }

      it("does not render for a false value") {
        mustacheRenderer.render(
          """Shown.
            |{{#person}}
            |  Never shown!
            |{{/person}}""".stripMargin, Map("person" -> false)) should be(Right(
          """Shown.
            |""".stripMargin
        ))
      }

      it("does not render for an empty iterable") {
        mustacheRenderer.render(
          """Shown.
            |{{#person}}
            |  Never shown!
            |{{/person}}""".stripMargin, Map("person" -> Seq.empty)) should be(Right(
          """Shown.
            |""".stripMargin
        ))
      }

      it("renders the inner template for each item of the list") {
        mustacheRenderer.render(
            template =
              """{{#repo}}
                |  <b>{{name}}</b>
                |{{/repo}}""".stripMargin,
            context = Map("repo" -> Seq(
              Map("name" -> "resque"),
              Map("name" -> "hub"),
              Map("name" -> "rip")
            ))) should be(Right(
            """
              |  <b>resque</b>
              |
              |  <b>hub</b>
              |
              |  <b>rip</b>
              |""".stripMargin
            ))
        }

      it("invokes the lambda with the unprocessed template and a render method") {
        val wrapped: Lambda = (template, render) => Right(s"<b>${render(template).right.get}</b>")
        mustacheRenderer.render(
            template = """{{#wrapped}}
              |  {{name}} is awesome.
              |{{/wrapped}}""".stripMargin,
            context = Map(
              "name" -> "Willy",
              "wrapped" -> wrapped)
          ) should be(Right(
          """<b>
            |  Willy is awesome.
            |</b>""".stripMargin
          ))
        }

      it("for a non-false, non-iterable value uses the value as the context for a rendering of the section template") {
        mustacheRenderer.render(
            template =
              """{{#person?}}
                |  Hi {{name}}!
                |{{/person?}}""".stripMargin,
            context = Map("person?" -> Map("name" -> "Jon"))
          ) should be(Right(
          """
            |  Hi Jon!
            |""".stripMargin
          ))
        }

    }

    describe("an inverted section tag") {
      pending

      it("renders once when the key doesn't exist") {
        mustacheRenderer.render("{{^name}}No name!{{/name}}") should be(Right("No name!"))
      }

      it("renders once when the key is a false value") {
        mustacheRenderer.render("{{^else}}do this{{/else}}", Map("else" -> false)) should be(Right("do this"))
      }

      it("renders once when the key is an empty list") {
        mustacheRenderer.render(
          template =
            """{{#repo}}
              |  <b>{{name}}</b>
              |{{/repo}}
              |{{^repo}}
              |  No repos! :(
              |{{/repo}}""".stripMargin,
          context = Map("repo" -> List.empty)) should be(Right(
            """
              |
              |  No repos! :(
              |""".stripMargin))
      }

    }

    describe("a comment") {
      pending

      it("is not rendered") {
        mustacheRenderer.render("""<h1>Today{{! ignore me }}.</h1>""") should be(Right("<h1>Today.</h1>"))
      }

      it("may contain newlines") {
        mustacheRenderer.render(
          """{{!
            |If you can read this, something went wrong
            |}}""".stripMargin) should be(Right(""))
      }

    }

    describe("a partial") {
      pending

      it("is rendered once in the current context") {
        mustacheRenderer.withTemplates("user" -> "<strong>{{name}}</strong>").render(
          template = """<h2>Names</h2>
                       |{{#names}}
                       |  {{> user}}
                       |{{/names}}""".stripMargin,
          context = Map("names" -> Seq(Map("name" -> "Jennifer")))
        ) should be(Right(
          """<h2>Names</h2>
            |
            |  <strong>Jennifer</strong>
            |""".stripMargin))
      }

    }
  }

}