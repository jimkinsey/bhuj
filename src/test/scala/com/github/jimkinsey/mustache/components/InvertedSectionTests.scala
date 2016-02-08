package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache._
import org.scalatest.FunSpec
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar.mock
import org.scalatest.Matchers._

class InvertedSectionTests extends FunSpec {
  implicit val global: Context = Map.empty

  describe("An inverted section") {

    it("propagates a failure from the template") {
      val failing = mock[Template]
      when(failing.rendered(Map("section" -> false))).thenReturn(Left("BOOM"))
      new InvertedSection("section", failing).rendered(Map("section" -> false)) should be(Left("BOOM"))
    }

    it("renders the template once when the value is false") {
      new InvertedSection("section", Template(Text("a"))).rendered(Map("section" -> false)) should be(Right("a"))
    }

    it("renders the template once when the value is none") {
      new InvertedSection("section", Template(Text("a"))).rendered(Map("section" -> None)) should be(Right("a"))
    }

    it("renders nothing when the value is true") {
      new InvertedSection("section", Template(Text("a"))).rendered(Map("section" -> true)) should be(Right(""))
    }

    it("renders the template once when the value is an empty iterable") {
      new InvertedSection("section", Template(Text("a"))).rendered(Map("section" -> List.empty)) should be(Right("a"))
    }

    it("renders once when the key does not exist") {
      new InvertedSection("section", Template(Text("a"))).rendered(Map.empty) should be(Right("a"))
    }

  }

}
