package bhuj.rendering

import bhuj._
import bhuj.model._
import bhuj.parsing.Delimiters
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class RendererTests extends FunSpec {
  implicit val globalContext: Context = Map.empty

  describe("Rendering") {

    describe("A variable component") {

      it("returns an empty string if the key is not in the context") {
        renderer.rendered(Template(Variable("name")), Map("age" -> 37)) should be(Right(""))
      }

      it("returns the value from the context when present") {
        renderer.rendered(Template (Variable("name")), Map("name" -> "Jim")) should be(Right("Jim"))
      }

      it("converts the value to a string when it is not") {
        renderer.rendered(Template(Variable("age")), Map("age" -> 37)) should be(Right("37"))
      }

      it("escapes the value for HTML") {
        renderer.rendered(Template(Variable("fragment")), Map("fragment" -> """<blink>"&'</blink>""")) should be(Right("&lt;blink&gt;&quot;&amp;&#39;&lt;/blink&gt;"))
      }

    }

    describe("A triple-delimited variable") {

      it("returns an empty string if the key is not in the context") {
        renderer.rendered(Template(TripleDelimitedVariable("name")), Map("age" -> 37)) should be(Right(""))
      }

      it("returns the value from the context when present") {
        renderer.rendered(Template(TripleDelimitedVariable("name")), Map("name" -> "Jim")) should be(Right("Jim"))
      }

      it("converts the value to a string when it is not") {
        renderer.rendered(Template(TripleDelimitedVariable("age")), Map("age" -> 37)) should be(Right("37"))
      }

      it("does not escape the value for HTML") {
        renderer.rendered(Template(TripleDelimitedVariable("fragment")), Map("fragment" -> """<blink>"&'</blink>""")) should be(Right("""<blink>"&'</blink>"""))
      }

    }

    describe("A template") {

      it("renders to an empty string if it has no components") {
        renderer.rendered(Template(), emptyContext) should be(Right(""))
      }

      it("propagates the failure of any components") {
        val failing = Partial("non-existent")
        renderer.rendered(Template(failing), emptyContext) should be(Left(TemplateNotFound("non-existent")))
      }

      it("concatenates the results of rendering all its components") {
        val component1 = Text("X")
        val component2 = Text("Y")
        renderer.rendered(Template(component1, component2), emptyContext) should be(Right("XY"))
      }

      it("uses values in the global context") {
        pendingUntilFixed {
          val global = Map("x" -> 2, "y" -> 3)
          renderer.rendered(Template(Variable("x"), Variable("y")), Map("x" -> 1))(global) should be(Right("13"))
        }
      }

    }

    describe("A set delimiters directive") {

      it("renders to an empty string") {
        renderer.rendered(Template(SetDelimiters(Delimiters("[[", "]]"))), emptyContext) should be(Right(""))
      }

    }

    describe("A section component") {

      it("does not render when the key is not in the context") {
        renderer.rendered(Template(Section("things", Template())), emptyContext) should be(Right(""))
      }

      it("does not render if the named value in the context is false") {
        renderer.rendered(Template(Section("doIt", Template())), Map("doIt" -> false)) should be(Right(""))
      }

      it("does not render if the named value in the context is an empty iterable") {
        renderer.rendered(Template(Section("things", Template())), Map("things" -> List.empty)) should be(Right(""))
      }

      it("does not render if the named value in the context is an undefined option") {
        renderer.rendered(Template(Section("maybe", Template(Text("a")))), Map("Maybe" -> None)) should be(Right(""))
      }

      it("returns the failure if the named value is a lambda which fails") {
        val failure = LambdaFailure("key", "#fail")
        val failingLambda: Lambda = (_, _) => Left(failure)
        renderer.rendered(Template(Section("wrap", Template())), Map("wrap" -> failingLambda)) should be(Left(LambdaFailure("wrap", failure)))
      }

      it("returns the failure if the named value is a non-false value which fails to render") {
        val failing = Template(Partial("failing"))
        renderer.rendered(Template(Section("thing", failing)), Map("thing" -> Map("a" -> 1))) should be(Left(TemplateNotFound("failing")))
      }

      it("renders the section once using the value as a context if it is a map") {
        val template = Template(Text("a"))
        val section = Section("section", template)
        renderer.rendered(Template(section), Map("section" -> Map("a" -> 1))) should be(Right(s"a"))
      }

      it("renders the section once in the current context if it is true") {
        val template = Template(Text("a"))
        renderer.rendered(Template(Section("doIt", template)), Map("doIt" -> true)) should be(Right("a"))
      }

      it("renders the section once in the current context if the item is a defined option") {
        val template = Template(Text("a"))
        renderer.rendered(Template(Section("maybe", template)), Map("maybe" -> Some(Map.empty))) should be(Right("a"))
      }

      it("renders the section for each item in a non-empty iterable with the item as the context") {
        val template = Template(Text("a"))
        renderer.rendered(Template(Section("things", template)), Map("things" -> List(Map.empty, Map.empty, Map.empty))) should be(Right(s"aaa"))
      }

      it("renders the section once for a lambda") {
        val template = Template(Text("a"))
        val lambda: Lambda = (template, rendered) => Right(s"LAMBDA'D: ${rendered(template).right.get}")
        renderer.rendered(Template(Section("wrap", template)), Map("wrap" -> lambda)) should be(Right(s"LAMBDA'D: a"))
      }

    }

    describe("A partial component") {

      it("propagates failure to render the template") {
        val renderer = new Renderer(
          new ScalaConverter,
          new TemplateCompiler,
          new Mustache().parse,
          {
            case "partial" => Some("{{> no-such-partial}}")
            case _ => None
          }
        )
        renderer.rendered(Template(Partial("partial")), Map("foo" -> 42)) should be(Left(TemplateNotFound("no-such-partial")))
      }

      it("renders the named template in the provided context") {
        val renderer = new Renderer(
          new ScalaConverter,
          new TemplateCompiler,
          new Mustache().parse,
          {
            case "partial" => Some("{{foo}}")
            case _ => None
          }
        )
        renderer.rendered(Template(Partial("partial")), Map("foo" -> 42)) should be(Right("42"))
      }

    }

    describe("An inverted section") {

      it("propagates a failure from the template") {
        pendingUntilFixed {
          val failing = Template(Partial("failing"))
          renderer.rendered(Template(InvertedSection("section", failing)), Map("section" -> false)) should be(Left(TemplateNotFound("failing")))
        }
      }

      it("renders the template once when the value is false") {
        pendingUntilFixed {
          renderer.rendered(Template(InvertedSection("section", Template(Text("a")))), Map("section" -> false)) should be(Right("a"))
        }
      }

      it("renders the template once when the value is none") {
        pendingUntilFixed {
          renderer.rendered(Template(InvertedSection("section", Template(Text("a")))), Map("section" -> None)) should be(Right("a"))
        }
      }

      it("renders nothing when the value is true") {
        renderer.rendered(Template(InvertedSection("section", Template(Text("a")))), Map("section" -> true)) should be(Right(""))
      }

      it("renders the template once when the value is an empty iterable") {
        pendingUntilFixed {
          renderer.rendered(Template(InvertedSection("section", Template(Text("a")))), Map("section" -> List.empty)) should be(Right("a"))
        }
      }

      it("renders once when the key does not exist") {
        pendingUntilFixed {
          renderer.rendered(Template(InvertedSection("section", Template(Text("a")))), emptyContext) should be(Right("a"))
        }
      }

    }

  }

  private lazy val renderer = new Renderer(new ScalaConverter, new TemplateCompiler, new Mustache().parse, _ => None)

}
