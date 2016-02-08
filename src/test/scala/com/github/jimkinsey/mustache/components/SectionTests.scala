package com.github.jimkinsey.mustache.components

import org.mockito.Mockito.when
import org.scalatest.FunSpec
import org.scalatest.Matchers._
import org.scalatest.mock.MockitoSugar.mock

class SectionTests extends FunSpec {

  describe("A section component") {

    it("does not render when the key is not in the context") {
      new Section("things", Template()).rendered(Map.empty) should be(Right(""))
    }

    it("does not render if the named value in the context is false") {
      new Section("doIt", Template()).rendered(Map("doIt" -> false)) should be(Right(""))
    }

    it("does not render if the named value in the context is an empty iterable") {
      new Section("things", Template()).rendered(Map("things" -> List.empty)) should be(Right(""))
    }

    it("returns the failure if the named value is a lambda which fails") {
      val failingLambda: Section.Lambda = (_, _) => Left("lol")
      new Section("wrap", Template()).rendered(Map("wrap" -> failingLambda)) should be(Left("lol"))
    }

    it("returns the failure if the named value is a non-false value which fails to render") {
      val failing = mock[Template]
      when(failing.rendered(Map("a" -> 1))).thenReturn(Left("lol"))
      new Section("thing", failing).rendered(Map("thing" -> Map("a" -> 1))) should be(Left("lol"))
    }

    it("renders the section once using the value as a context if it is a map") {
      val template = Template(Text("a"))
      val section = new Section("section", template)
      section.rendered(Map("section" -> Map("a" -> 1))) should be(Right(s"a"))
    }

    it("renders the section once in the current context if it is true") {
      val template = Template(Text("a"))
      new Section("doIt", template).rendered(Map("doIt" -> true)) should be(Right("a"))
    }

    it("renders the section for each item in a non-empty iterable with the item as the context") {
      val template = Template(Text("a"))
      new Section("things", template).rendered(Map("things" -> List(Map.empty,Map.empty,Map.empty))) should be (Right(s"aaa"))
    }

    it("renders the section once for a lambda") {
      val template = Template(Text("a"))
      val lambda: Section.Lambda = (template, rendered) => Right(s"LAMBDA'D: ${rendered(template).right.get}")
      new Section("wrap", template).rendered(Map("wrap" -> lambda)) should be(Right(s"LAMBDA'D: a"))
    }
  }

}
