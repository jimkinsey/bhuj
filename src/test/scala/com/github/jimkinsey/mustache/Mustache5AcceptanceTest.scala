package com.github.jimkinsey.mustache

import org.scalatest.FunSpec
import org.scalatest.Matchers._



class Mustache5AcceptanceTest extends FunSpec {

  describe("Mustache") {

    describe("a variable tag") {

      it("is replaced by an empty string when the key is not in the context") {
        new Mustache().render("Hello {{name}}", Map.empty) should be(Right("Hello "))
      }

      it("is replaced by the value from the context when present") {
        new Mustache().render("Hello {{name}}", Map("name" -> "Chris")) should be(Right("Hello Chris"))
      }

      it("escapes for HTML by default") {
        new Mustache().render("{{html}}", Map("html" -> """<blink>"&'</blink>""")) should be(Right("&lt;blink&gt;&quot;&amp;&#39;&lt;/blink&gt;"))
      }

      it("does not escape when the variable is triple-delimited") {
        new Mustache().render("{{{html}}}", Map("html" -> """<blink>"&'</blink>""")) should be(Right("""<blink>"&'</blink>"""))
      }

    }

    describe("a section tag") {

      it("does not render when the key is not in the context") {
        new Mustache().render("before:{{#x}}X{{/x}}:after") should be(Right("before::after"))
      }

      it("does not render for a false value") {
        new Mustache().render(
          """Shown.
            |{{#person}}
            |  Never shown!
            |{{/person}}""".stripMargin, Map("person" -> false)) should be(Right(
          """Shown.
            |""".stripMargin
        ))
      }

      it("does not render for an empty iterable") {
        new Mustache().render(
          """Shown.
            |{{#person}}
            |  Never shown!
            |{{/person}}""".stripMargin, Map("person" -> Seq.empty)) should be(Right(
          """Shown.
            |""".stripMargin
        ))
      }

      it("renders the inner template for each item of the list") {
          new Mustache().render(
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
          new Mustache().render(
            template = """{{#wrapped}}
              |  {{name}} is awesome.
              |{{/wrapped}}""".stripMargin,
            context = Map(
              "name" -> "Willy",
              "wrapped" -> { (template: String, render: (String => Either[Renderer.Failure, String])) => Right(s"<b>${render(template).right.get}</b>") })
          ) should be(Right(
          """<b>
            |  Willy is awesome.
            |</b>""".stripMargin
          ))
        }

      it("for a non-false, non-iterable value uses the value as the context for a rendering of the section template") {
          new Mustache().render(
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

      it("renders once when the key doesn't exist") {
        new Mustache().render("{{^name}}No name!{{/name}}") should be(Right("No name!"))
      }

      it("renders once when the key is a false value") {
        new Mustache().render("{{^else}}do this{{/else}}", Map("else" -> false)) should be(Right("do this"))
      }

      it("renders once when the key is an empty list") {
        new Mustache().render(
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

      it("is not rendered") {
        new Mustache().render("""<h1>Today{{! ignore me }}.</h1>""") should be(Right("<h1>Today.</h1>"))
      }

      it("may contain newlines") {
        new Mustache().render(
          """{{!
            |If you can read this, something went wrong
            |}}""".stripMargin) should be(Right(""))
      }

    }

    describe("a partial") {

      it("is rendered once in the current context") {
        new Mustache(templates = Map("user" -> "<strong>{{name}}</strong>")).render(
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