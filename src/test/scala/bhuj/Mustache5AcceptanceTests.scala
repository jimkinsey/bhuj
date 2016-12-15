package bhuj

import bhuj.context.ContextImplicits
import org.scalatest.AsyncFunSpec
import org.scalatest.Matchers._

class Mustache5AcceptanceTests extends AsyncFunSpec {
  import ContextImplicits._
  import MustacheBuilder.mustacheRenderer

  describe("Mustache") {

    describe("a variable tag") {

      it("is replaced by an empty string when the key is not in the context") {
        mustacheRenderer.render("Hello {{name}}", Map[String,Any]()) map (_ shouldBe Right("Hello "))
      }

      it("is replaced by the value from the context when present") {
        mustacheRenderer.render("Hello {{name}}", Map("name" -> "Chris")) map (_ shouldBe Right("Hello Chris"))
      }

      it("escapes for HTML by default") {
        mustacheRenderer.render("{{html}}", Map("html" -> """<blink>"&'</blink>""")) map (_ shouldBe Right("&lt;blink&gt;&quot;&amp;&#39;&lt;/blink&gt;"))
      }

      it("does not escape when the variable is triple-delimited") {
        mustacheRenderer.render("{{{html}}}", Map("html" -> """<blink>"&'</blink>""")) map (_ shouldBe Right("""<blink>"&'</blink>"""))
      }

      it("does not escape when the variable starts with an ampersand") {
        mustacheRenderer.render("{{&html}}", Map("html" -> """<blink>"&'</blink>""")) map (_ shouldBe Right("""<blink>"&'</blink>"""))
      }

    }

    describe("a section tag") {

      it("does not render when the key is not in the context") {
        mustacheRenderer.render("before:{{#x}}X{{/x}}:after") map (_ shouldBe Right("before::after"))
      }

      it("does not render for a false value") {
        mustacheRenderer.render(
          """Shown.
            |{{#person}}
            |  Never shown!
            |{{/person}}""".stripMargin, Map("person" -> false)) map (_ shouldBe Right(
          """Shown.
            |""".stripMargin
        ))
      }

      it("does not render for an empty iterable") {
        mustacheRenderer.render(
          """Shown.
            |{{#person}}
            |  Never shown!
            |{{/person}}""".stripMargin, Map("person" -> Seq.empty)) map (_ shouldBe Right(
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
            ))) map (_ shouldBe Right(
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
        val wrapped: Lambda = (template, render) => render(template) map (_ map (res => s"<b>$res</b>"))
        mustacheRenderer.render(
            template = """{{#wrapped}}
              |  {{name}} is awesome.
              |{{/wrapped}}""".stripMargin,
            context = Map(
              "name" -> "Willy",
              "wrapped" -> wrapped)
          ) map (_ shouldBe Right(
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
          ) map (_ shouldBe Right(
          """
            |  Hi Jon!
            |""".stripMargin
          ))
        }

    }

    describe("an inverted section tag") {

      it("renders once when the key doesn't exist") {
        mustacheRenderer.render("{{^name}}No name!{{/name}}") map (_ shouldBe Right("No name!"))
      }

      it("renders once when the key is a false value") {
        mustacheRenderer.render("{{^else}}do this{{/else}}", Map("else" -> false)) map (_ shouldBe Right("do this"))
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
          context = Map("repo" -> List.empty)) map (_ shouldBe Right(
            """
              |
              |  No repos! :(
              |""".stripMargin))
      }

    }

    describe("a comment") {

      it("is not rendered") {
        mustacheRenderer.render("""<h1>Today{{! ignore me }}.</h1>""") map (_ shouldBe Right("<h1>Today.</h1>"))
      }

      it("may contain newlines") {
        mustacheRenderer.render(
          """{{!
            |If you can read this, something went wrong
            |}}""".stripMargin) map (_ shouldBe Right(""))
      }

    }

    describe("a partial") {

      it("is rendered once in the current context") {
        mustacheRenderer.withTemplates("user" -> "<strong>{{name}}</strong>").render(
          template = """<h2>Names</h2>
                       |{{#names}}
                       |  {{> user}}
                       |{{/names}}""".stripMargin,
          context = Map("names" -> Seq(Map("name" -> "Jennifer")))
        ) map (_ shouldBe Right(
          """<h2>Names</h2>
            |
            |  <strong>Jennifer</strong>
            |""".stripMargin))
      }

      it("is rendered at runtime and so may be recursive") {
        mustacheRenderer
          .withTemplates("child" -> "{{name}} {{#child}}{{> child}}{{/child}}")
          .renderTemplate("child", Map(
            "name" -> "Grandma",
            "child" -> Map(
              "name" -> "Mum",
              "child" -> Map("name" -> "Me")
          ))) map (_ shouldBe Right(
            "Grandma Mum Me "
          ))
      }
    }

    describe("set delimiters") {

      it("changes the tag delimiters to custom strings") {
        mustacheRenderer
          .render(
            """* {{default_tags}}
            |{{=<% %>=}}
            |* <% erb_style_tags %>
            |<%={{ }}=%>
            |* {{ default_tags_again }}
            |""".
              stripMargin,
          context = Map(
            "default_tags" -> 1,
            "erb_style_tags" -> 2,
            "default_tags_again" -> 3
          )) map (_ shouldBe Right( """* 1
              |
              |* 2
              |
              |* 3
              |""".stripMargin
          ))
      }

      it("may not use whitespace or the equals sign in the delimiters") {
        mustacheRenderer.render("{{=<= = >=}}") map (_ shouldBe Left(InvalidDelimiters("<=", "= >")))
      }

    }
  }
}