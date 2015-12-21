package com.github.jimkinsey

import com.github.jimkinsey.SectionStartTag.UnclosedSection
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class MustacheTest extends FunSpec {

  describe("Mustache") {

    it("leaves a string containing no tags untouched") {
      new Mustache().render("No tags") should be(Right("No tags"))
    }

    it("leaves an empty string untouched") {
      new Mustache().render("") should be(Right(""))
    }

    it("works for a multi-line template") {
      new Mustache().render(
        """1: {{one}},
          |2: {{two}},
          |3: {{three}}""".stripMargin, Map("one" -> 1, "two" -> 2, "three" -> 3)) should be(Right(
        """1: 1,
          |2: 2,
          |3: 3""".stripMargin))
    }

    describe("a variable tag") {

      it("is replaced by an empty string when the key is not in the context") {
        new Mustache().render("Hello {{name}}", Map.empty) should be(Right("Hello "))
      }

      it("is replaced by the value from the context when present") {
        new Mustache().render("Hello {{name}}", Map("name" -> "Chris")) should be(Right("Hello Chris"))
      }

      it("works with multiple variables") {
        new Mustache().render("Hi {{first}} {{last}}", Map("first" -> "John", "last" -> "Smith")) should be(Right("Hi John Smith"))
      }

      it("escapes for HTML by default") {
        new Mustache().render("{{html}}", Map("html" -> """<blink>"&'</blink>""")) should be(Right("&lt;blink&gt;&quot;&amp;&#39;&lt;/blink&gt;"))
      }

      it("does not escape when the variable is triple-delimited") {
        new Mustache().render("{{{html}}}", Map("html" -> """<blink>"&'</blink>""")) should be(Right("""<blink>"&'</blink>"""))
      }

      it("may have a one character name") {
        new Mustache().render("{{x}}", Map("x" -> "X")) should be(Right("""X"""))
      }

      it("may have a one character name for a non-escaped variable") {
        new Mustache().render("{{{x}}}", Map("x" -> "X")) should be(Right("""X"""))
      }

    }

    describe("a section tag") {

      it("returns an error when the closing tag is not found") {
        new Mustache().render("{{#opened}}but never closed...") should be(Left(UnclosedSection("opened")))
      }

      it("does not render when the key is not in the context") {
        new Mustache().render("before:{{#x}}X{{/x}}:after") should be(Right("before::after"))
      }

      describe("for a false value") {

        it("does not render") {
          new Mustache().render(
            """Shown.
              |{{#person}}
              |  Never shown!
              |{{/person}}""".stripMargin, Map("person" -> false)) should be(Right(
            """Shown.
              |""".stripMargin
          ))
        }

      }

      describe("for an empty iterable") {

        it("does not render") {
          new Mustache().render(
            """Shown.
              |{{#person}}
              |  Never shown!
              |{{/person}}""".stripMargin, Map("person" -> Seq.empty)) should be(Right(
            """Shown.
              |""".stripMargin
          ))
        }

      }

      describe("for a non-empty iterable") {

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

      }

      describe("for a function") {

        it("invokes the function with the unprocessed template and the render method") {
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

      }

      describe("for a non-false, non-iterable value") {

        it("uses the value as the context for a rendering of the section template") {
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

    }
  }
}