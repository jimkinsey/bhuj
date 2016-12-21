package bhuj.rendering

import bhuj._
import bhuj.model._
import bhuj.parsing.Delimiters
import org.scalatest.AsyncFunSpec
import org.scalatest.Matchers._

import scala.concurrent.Future

class RendererTests extends AsyncFunSpec {
  implicit val globalContext: Context = Map.empty

  describe("Rendering") {

    describe("A variable component") {

      it("returns an empty string if the key is not in the context") {
        renderer.rendered(Template(Variable("name")), Map("age" -> 37)) map (_ shouldBe Right(""))
      }

      it("returns the value from the context when present") {
        renderer.rendered(Template(Variable("name")), Map("name" -> "Jim")) map (_ shouldBe Right("Jim"))
      }

      it("converts the value to a string when it is not") {
        renderer.rendered(Template(Variable("age")), Map("age" -> 37)) map (_ shouldBe Right("37"))
      }

      it("escapes the value for HTML") {
        renderer.rendered(Template(Variable("fragment")), Map("fragment" -> """<blink>"&'</blink>""")) map (_ shouldBe Right("&lt;blink&gt;&quot;&amp;&#39;&lt;/blink&gt;"))
      }

    }

    describe("A triple-delimited variable") {

      it("returns an empty string if the key is not in the context") {
        renderer.rendered(Template(TripleDelimitedVariable("name")), Map("age" -> 37)) map (_ shouldBe Right(""))
      }

      it("returns the value from the context when present") {
        renderer.rendered(Template(TripleDelimitedVariable("name")), Map("name" -> "Jim")) map (_ shouldBe Right("Jim"))
      }

      it("converts the value to a string when it is not") {
        renderer.rendered(Template(TripleDelimitedVariable("age")), Map("age" -> 37)) map (_ shouldBe Right("37"))
      }

      it("does not escape the value for HTML") {
        renderer.rendered(Template(TripleDelimitedVariable("fragment")), Map("fragment" -> """<blink>"&'</blink>""")) map (_ shouldBe Right("""<blink>"&'</blink>"""))
      }

    }

    describe("A template") {

      it("renders to an empty string if it has no components") {
        renderer.rendered(Template(), emptyContext) map (_ shouldBe Right(""))
      }

      it("propagates the failure of any components") {
        val failing = Partial("non-existent")
        renderer.rendered(Template(failing), emptyContext) map (_ shouldBe Left(TemplateNotFound("non-existent")))
      }

      it("concatenates the results of rendering all its components") {
        val component1 = Text("X")
        val component2 = Text("Y")
        renderer.rendered(Template(component1, component2), emptyContext) map (_ shouldBe Right("XY"))
      }

      it("uses values in the global context") {
        val global = Map("x" -> 2, "y" -> 3)
        renderer.rendered(Template(Variable("x"), Variable("y")), Map("x" -> 1))(global, executionContext) map (_ shouldBe Right("13"))
      }

      it("makes the global context available to sub-templates") {
        val global: Context = Map("x" -> 2, "xxx" -> true)
        renderer.rendered(Template(Section("xxx", Template(Variable("x")))), Map())(global, executionContext) map (_ shouldBe Right("2"))
      }

    }

    describe("A set delimiters directive") {

      it("renders to an empty string") {
        renderer.rendered(Template(SetDelimiters(Delimiters("[[", "]]"))), emptyContext) map (_ shouldBe Right(""))
      }

    }

    describe("A section component") {

      it("does not render when the key is not in the context") {
        renderer.rendered(Template(Section("things", Template())), emptyContext) map (_ shouldBe Right(""))
      }

      it("does not render if the named value in the context is false") {
        renderer.rendered(Template(Section("doIt", Template())), Map("doIt" -> false)) map (_ shouldBe Right(""))
      }

      it("does not render if the named value in the context is an empty iterable") {
        renderer.rendered(Template(Section("things", Template())), Map("things" -> List.empty)) map (_ shouldBe Right(""))
      }

      it("does not render if the named value in the context is an undefined option") {
        renderer.rendered(Template(Section("maybe", Template(Text("a")))), Map("Maybe" -> None)) map (_ shouldBe Right(""))
      }

      it("returns the failure if the named value is a lambda which fails") {
        val failure = LambdaFailure("key", "#fail")
        val failingLambda: Lambda = (_, _) => Future.successful(Left(failure))
        renderer.rendered(Template(Section("wrap", Template())), Map("wrap" -> failingLambda)) map (_ shouldBe Left(LambdaFailure("wrap", failure)))
      }

      it("returns the failure if the named value is a non-false value which fails to render") {
        val failing = Template(Partial("failing"))
        renderer.rendered(Template(Section("thing", failing)), Map("thing" -> Map("a" -> 1))) map (_ shouldBe Left(TemplateNotFound("failing")))
      }

      it("renders the section once using the value as a context if it is a map") {
        val template = Template(Text("a"))
        val section = Section("section", template)
        renderer.rendered(Template(section), Map("section" -> Map("a" -> 1))) map (_ shouldBe Right(s"a"))
      }

      it("renders the section once in the current context if it is true") {
        val template = Template(Text("a"))
        renderer.rendered(Template(Section("doIt", template)), Map("doIt" -> true)) map (_ shouldBe Right("a"))
      }

      it("renders the section once in the current context if the item is a defined option") {
        val template = Template(Text("a"))
        renderer.rendered(Template(Section("maybe", template)), Map("maybe" -> Some(Map.empty))) map (_ shouldBe Right("a"))
      }

      it("renders the section for each item in a non-empty iterable with the item as the context") {
        val template = Template(Text("a"))
        renderer.rendered(Template(Section("things", template)), Map("things" -> List(Map.empty, Map.empty, Map.empty))) map (_ shouldBe Right(s"aaa"))
      }

      it("renders the section once for a lambda") {
        val template = Template(Text("a"))
        val lambda: Lambda = (template, rendered) => rendered(template) map (_ map (res => s"LAMBDA'D: $res"))
        renderer.rendered(Template(Section("wrap", template)), Map("wrap" -> lambda)) map (_ shouldBe Right(s"LAMBDA'D: a"))
      }

    }

    describe("A partial component") {

      it("propagates failure to render the template") {
        val renderer = new Renderer(
          new Mustache().parse,
          {
            case "partial" => Future successful Some("{{> no-such-partial}}")
            case _         => Future successful None
          }
        )
        renderer.rendered(Template(Partial("partial")), Map("foo" -> 42)) map (_ shouldBe Left(TemplateNotFound("no-such-partial")))
      }

      it("renders the named template in the provided context") {
        val renderer = new Renderer(
          new Mustache().parse,
          {
            case "partial" => Future successful Some("{{foo}}")
            case _         => Future successful None
          }
        )
        renderer.rendered(Template(Partial("partial")), Map("foo" -> 42)) map (_ shouldBe Right("42"))
      }

    }

    describe("An inverted section") {

      it("propagates a failure from the template") {
        val failing = Template(Partial("failing"))
        renderer.rendered(Template(InvertedSection("section", failing)), Map("section" -> false)) map (_ shouldBe Left(TemplateNotFound("failing")))
      }

      it("renders the template once when the value is false") {
        renderer.rendered(Template(InvertedSection("section", Template(Text("a")))), Map("section" -> false)) map (_ shouldBe Right("a"))
      }

      it("renders the template once when the value is none") {
        renderer.rendered(Template(InvertedSection("section", Template(Text("a")))), Map("section" -> None)) map (_ shouldBe Right("a"))
      }

      it("renders nothing when the value is true") {
        renderer.rendered(Template(InvertedSection("section", Template(Text("a")))), Map("section" -> true)) map (_ shouldBe Right(""))
      }

      it("renders the template once when the value is an empty iterable") {
        renderer.rendered(Template(InvertedSection("section", Template(Text("a")))), Map("section" -> List.empty)) map (_ shouldBe Right("a"))
      }

      it("renders once when the key does not exist") {
        renderer.rendered(Template(InvertedSection("section", Template(Text("a")))), emptyContext) map (_ shouldBe Right("a"))
      }

    }

  }

  private lazy val renderer = new Renderer(new Mustache().parse, _ => Future successful None)

}
