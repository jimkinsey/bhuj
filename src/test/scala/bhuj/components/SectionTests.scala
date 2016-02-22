package bhuj.components

import bhuj._
import bhuj.parsing.Delimiters
import org.mockito.Mockito.when
import org.scalatest.FunSpec
import org.scalatest.Matchers._
import org.scalatest.mock.MockitoSugar.mock

class SectionTests extends FunSpec {
  implicit val global: Context = Map.empty

  describe("A section component") {

    it("does not render when the key is not in the context") {
      new Section("things", Template(), render).rendered(Map.empty) should be(Right(""))
    }

    it("does not render if the named value in the context is false") {
      new Section("doIt", Template(), render).rendered(Map("doIt" -> false)) should be(Right(""))
    }

    it("does not render if the named value in the context is an empty iterable") {
      new Section("things", Template(), render).rendered(Map("things" -> List.empty)) should be(Right(""))
    }

    it("does not render if the named value in the context is an undefined option") {
      new Section("maybe", Template(Text("a")), render).rendered(Map("Maybe" -> None)) should be(Right(""))
    }

    it("returns the failure if the named value is a lambda which fails") {
      val failure = mock[Failure]
      val failingLambda: Lambda = (_, _) => Left(failure)
      new Section("wrap", Template(), render).rendered(Map("wrap" -> failingLambda)) should be(Left(LambdaFailure("wrap", failure)))
    }

    it("returns the failure if the named value is a non-false value which fails to render") {
      val failure = mock[Failure]
      val failing = mock[Template]
      when(failing.rendered(Map("a" -> 1))).thenReturn(Left(failure))
      new Section("thing", failing, render).rendered(Map("thing" -> Map("a" -> 1))) should be(Left(failure))
    }

    it("renders the section once using the value as a context if it is a map") {
      val template = Template(Text("a"))
      val section = new Section("section", template, render)
      section.rendered(Map("section" -> Map("a" -> 1))) should be(Right(s"a"))
    }

    it("renders the section once in the current context if it is true") {
      val template = Template(Text("a"))
      new Section("doIt", template, render).rendered(Map("doIt" -> true)) should be(Right("a"))
    }

    it("renders the section once in the current context if the item is a defined option") {
      val template = Template(Text("a"))
      new Section("maybe", template, render).rendered(Map("maybe" -> Some(Map.empty))) should be(Right("a"))
    }

    it("renders the section for each item in a non-empty iterable with the item as the context") {
      val template = Template(Text("a"))
      new Section("things", template, render).rendered(Map("things" -> List(Map.empty,Map.empty,Map.empty))) should be (Right(s"aaa"))
    }

    it("renders the section once for a lambda") {
      val template = Template(Text("a"))
      val render: (String, Context) => Result = (str, ctx) => Right(s"Rendered: $str")
      val lambda: Lambda = (template, rendered) => Right(s"LAMBDA'D: ${rendered(template).right.get}")
      new Section("wrap", template, render).rendered(Map("wrap" -> lambda)) should be(Right(s"LAMBDA'D: Rendered: a"))
    }

    it("formats as the tags with the inner template formatted between them") {
      new Section("name", Template(Text("text")), render).formatted(Delimiters("{{", "}}")) should be("{{#name}}text{{/name}}")
    }
  }

  private val render: (String, Context) => Result = (str, ctx) => Right(s"Rendered: $str")

}
