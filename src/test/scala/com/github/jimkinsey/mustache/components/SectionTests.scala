package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.rendering.Template
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class SectionTests extends FunSpec {

  describe("A section component") {

    it("does not render when the key is not in the context") {
      new Section("things", Template()).rendered(Map.empty, render) should be(Right(""))
    }

    it("does not render if the named value in the context is false") {
      new Section("doIt", Template()).rendered(Map("doIt" -> false), render) should be(Right(""))
    }

    it("does not render if the named value in the context is an empty iterable") {
      new Section("things", Template()).rendered(Map("things" -> List.empty), render) should be(Right(""))
    }

    it("returns the failure if the named value is a lambda which fails") {
      val failingLambda: Section.Lambda = (_, _) => Left("lol")
      new Section("wrap", Template()).rendered(Map("wrap" -> failingLambda), render) should be(Left("lol"))
    }

    it("returns the failure if the named value is a non-false value which fails to render") {
      val failingRender: Section.Render = (_, _) => Left("lol")
      new Section("thing", Template()).rendered(Map("thing" -> Map("a" -> 1)), failingRender) should be(Left("lol"))
    }

    it("renders the section once using the value as a context if it is a map") {
      val template = Template(Text("a"))
      val section = new Section("section", template)
      val render: Section.Render = (template, context) => Right(context("a").toString + template.toString)
      section.rendered(Map("section" -> Map("a" -> 1)), render) should be(Right(s"1$template"))
    }

    it("renders the section once in the current context if it is true") {
      val template = Template(Text("a"))
      val render: Section.Render = (template, context) => Right(template.toString)
      new Section("doIt", template).rendered(Map("doIt" -> true), render) should be(Right(template.toString))
    }

    it("renders the section for each item in a non-empty iterable with the item as the context") {
      val template = Template(Text("a"))
      val render: Section.Render = (template, context) => Right(template.toString)
      new Section("things", template).rendered(Map("things" -> List(Map.empty,Map.empty,Map.empty)), render) should be (Right(s"$template$template$template"))
    }

    it("renders the section once for a lambda") {
      val template = Template(Text("a"))
      val render: Section.Render = (template, _) => Right(template.toString)
      val lambda: Section.Lambda = (template, rendered) => Right(s"LAMBDA'D: ${rendered(template).right.get}")
      new Section("wrap", template).rendered(Map("wrap" -> lambda), render) should be(Right(s"LAMBDA'D: $template"))
    }
  }

  private val render: Section.Render = (a,b) => ???

}
