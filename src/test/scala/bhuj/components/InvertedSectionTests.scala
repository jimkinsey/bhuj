package bhuj.components

import bhuj._
import org.mockito.Mockito.when
import org.scalatest.FunSpec
import org.scalatest.Matchers._
import org.scalatest.mock.MockitoSugar.mock

class InvertedSectionTests extends FunSpec {
  implicit val global: Context = Map.empty

  describe("An inverted section") {

    it("propagates a failure from the template") {
      val failing = mock[Template]
      val failure = mock[Failure]
      when(failing.rendered(Map("section" -> false))).thenReturn(Left(failure))
      new InvertedSection("section", failing, render).rendered(Map("section" -> false)) should be(Left(failure))
    }

    it("renders the template once when the value is false") {
      new InvertedSection("section", Template(Text("a")), render).rendered(Map("section" -> false)) should be(Right("a"))
    }

    it("renders the template once when the value is none") {
      new InvertedSection("section", Template(Text("a")), render).rendered(Map("section" -> None)) should be(Right("a"))
    }

    it("renders nothing when the value is true") {
      new InvertedSection("section", Template(Text("a")), render).rendered(Map("section" -> true)) should be(Right(""))
    }

    it("renders the template once when the value is an empty iterable") {
      new InvertedSection("section", Template(Text("a")), render).rendered(Map("section" -> List.empty)) should be(Right("a"))
    }

    it("renders once when the key does not exist") {
      new InvertedSection("section", Template(Text("a")), render).rendered(Map.empty) should be(Right("a"))
    }

  }

  private val render: (String, Context) => Result = (str, ctx) => Right(s"Rendered: $str")

}
