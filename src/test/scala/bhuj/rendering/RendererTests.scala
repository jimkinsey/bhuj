package bhuj.rendering

import bhuj._
import bhuj.model._
import bhuj.parsing.Delimiters
import org.scalatest.FunSpec
import org.scalatest.Matchers._
import org.scalatest.mock.MockitoSugar._

class RendererTests extends FunSpec {
  implicit val globalContext: Context = Map.empty

  describe("Rendering") {

    describe("A variable component") {

      it("returns an empty string if the key is not in the context") {
        renderer.rendered(Variable("name"), Map("age" -> 37)) should be(Right(""))
      }

      it("returns the value from the context when present") {
        renderer.rendered(Variable("name"), Map("name" -> "Jim")) should be(Right("Jim"))
      }

      it("overrides the value from the global context with the local") {
        val global = Map("name" -> "James")
        renderer.rendered(Variable("name"), Map("name" -> "Jim")) should be(Right("Jim"))
      }

      it("converts the value to a string when it is not") {
        renderer.rendered(Variable("age"), Map("age" -> 37)) should be(Right("37"))
      }

      it("escapes the value for HTML") {
        renderer.rendered(Variable("fragment"), Map("fragment" -> """<blink>"&'</blink>""")) should be(Right("&lt;blink&gt;&quot;&amp;&#39;&lt;/blink&gt;"))
      }

    }

    describe("A triple-delimited variable") {

      it("returns an empty string if the key is not in the context") {
        renderer.rendered(TripleDelimitedVariable("name"), Map("age" -> 37)) should be(Right(""))
      }

      it("returns the value from the context when present") {
        renderer.rendered(TripleDelimitedVariable("name"), Map("name" -> "Jim")) should be(Right("Jim"))
      }

      it("overrides the value from the global context with the local") {
        val global = Map("name" -> "James")
        renderer.rendered(TripleDelimitedVariable("name"), Map("name" -> "Jim")) should be(Right("Jim"))
      }

      it("converts the value to a string when it is not") {
        renderer.rendered(TripleDelimitedVariable("age"), Map("age" -> 37)) should be(Right("37"))
      }

      it("does not escape the value for HTML") {
        renderer.rendered(TripleDelimitedVariable("fragment"), Map("fragment" -> """<blink>"&'</blink>""")) should be(Right("""<blink>"&'</blink>"""))
      }

    }

    describe("A template") {

      it("renders to an empty string if it has no components") {
        renderer.rendered(Template(), emptyContext) should be(Right(""))
      }

      it("propagates the failure of any components") {
        val failing = Partial("failing", (_,_) => Left(failure))
        renderer.rendered(Template(failing), emptyContext) should be(Left(failure))
      }

      it("concatenates the results of rendering all its components") {
        val component1 = Text("X")
        val component2 = Text("Y")
        renderer.rendered(Template(component1, component2), emptyContext) should be(Right("XY"))
      }

      it("uses values in the global context") {
        val global = Map("x" -> 2, "y" -> 3)
        renderer.rendered(Template(Variable("x"), Variable("y")), Map("x" -> 1))(global) should be(Right("13"))
      }

    }

    describe("A set delimiters directive") {

      it("renders to an empty string") {
        renderer.rendered(SetDelimiters(Delimiters("[[", "]]")), emptyContext) should be(Right(""))
      }

    }

    describe("A section component") {

      it("does not render when the key is not in the context") {
        renderer.rendered(Section("things", Template(), render), emptyContext) should be(Right(""))
      }

      it("does not render if the named value in the context is false") {
        renderer.rendered(Section("doIt", Template(), render), Map("doIt" -> false)) should be(Right(""))
      }

      it("does not render if the named value in the context is an empty iterable") {
        renderer.rendered(Section("things", Template(), render), Map("things" -> List.empty)) should be(Right(""))
      }

      it("does not render if the named value in the context is an undefined option") {
        renderer.rendered(Section("maybe", Template(Text("a")), render), Map("Maybe" -> None)) should be(Right(""))
      }

      it("returns the failure if the named value is a lambda which fails") {
        val failingLambda: Lambda = (_, _) => Left(failure)
        renderer.rendered(Section("wrap", Template(), render), Map("wrap" -> failingLambda)) should be(Left(LambdaFailure("wrap", failure)))
      }

      it("returns the failure if the named value is a non-false value which fails to render") {
        val failing = Template(Partial("failing", (_,_) => Left(failure)))
        renderer.rendered(Section("thing", failing, render), Map("thing" -> Map("a" -> 1))) should be(Left(failure))
      }

      it("renders the section once using the value as a context if it is a map") {
        val template = Template(Text("a"))
        val section = Section("section", template, render)
        renderer.rendered(section, Map("section" -> Map("a" -> 1))) should be(Right(s"a"))
      }

      it("renders the section once in the current context if it is true") {
        val template = Template(Text("a"))
        renderer.rendered(Section("doIt", template, render), Map("doIt" -> true)) should be(Right("a"))
      }

      it("renders the section once in the current context if the item is a defined option") {
        val template = Template(Text("a"))
        renderer.rendered(Section("maybe", template, render), Map("maybe" -> Some(Map.empty))) should be(Right("a"))
      }

      it("renders the section for each item in a non-empty iterable with the item as the context") {
        val template = Template(Text("a"))
        renderer.rendered(Section("things", template, render), Map("things" -> List(Map.empty, Map.empty, Map.empty))) should be(Right(s"aaa"))
      }

      it("renders the section once for a lambda") {
        val template = Template(Text("a"))
        val lambda: Lambda = (template, rendered) => Right(s"LAMBDA'D: ${rendered(template).right.get}")
        renderer.rendered(Section("wrap", template, render), Map("wrap" -> lambda)) should be(Right(s"LAMBDA'D: Rendered: a"))
      }

    }

    describe("A partial component") {

      it("propagates failure to render the template") {
        val rendered: (String, Context) => Result = (_,_) => Left(failure)
        renderer.rendered(new Partial("partial", rendered), emptyContext) should be(Left(failure))
      }

      it("renders the named template in the provided context") {
        val rendered: (String, Context) => Result = (_,_) => Right("42")
        renderer.rendered(new Partial("partial", rendered), Map("foo" -> 42)) should be(Right("42"))
      }

    }

    describe("An inverted section") {

      it("propagates a failure from the template") {
        val failing = Template(Partial("failing", (_,_) => Left(failure)))
        renderer.rendered(InvertedSection("section", failing, render), Map("section" -> false)) should be(Left(failure))
      }

      it("renders the template once when the value is false") {
        renderer.rendered(InvertedSection("section", Template(Text("a")), render), Map("section" -> false)) should be(Right("a"))
      }

      it("renders the template once when the value is none") {
        renderer.rendered(InvertedSection("section", Template(Text("a")), render), Map("section" -> None)) should be(Right("a"))
      }

      it("renders nothing when the value is true") {
        renderer.rendered(InvertedSection("section", Template(Text("a")), render), Map("section" -> true)) should be(Right(""))
      }

      it("renders the template once when the value is an empty iterable") {
        renderer.rendered(InvertedSection("section", Template(Text("a")), render), Map("section" -> List.empty)) should be(Right("a"))
      }

      it("renders once when the key does not exist") {
        renderer.rendered(InvertedSection("section", Template(Text("a")), render), emptyContext) should be(Right("a"))
      }

    }

  }

  private lazy val renderer = new Renderer

  private val render: (String, Context) => Result = (str, ctx) => Right(s"Rendered: $str")

  private lazy val failure = mock[Failure]

}
