package com.github.jimkinsey.mustache.tags

import com.github.jimkinsey.mustache.rendering.Renderer
import Renderer.Context
import com.github.jimkinsey.mustache.tags.InvertedSection.UnclosedInvertedSection
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class InvertedSectionTest extends FunSpec {

  describe("An inverted section") {

    it("is defined by the tag content starting with a carat ('^')") {
      InvertedSection.pattern.findFirstMatchIn("^name") should be(defined)
    }

    it("returns the tag name") {
      InvertedSection.pattern.findFirstMatchIn("^name").get.group(1) should be("name")
    }

    it("does not match when the tag content does not begin with a carat") {
      InvertedSection.pattern.findFirstIn("name") should not be defined
    }

    it("fails with an UnclosedInvertedSection if no closing tag is found") {
      InvertedSection.process(
        "name",
        Map.empty,
        "remaining",
        unusedRender) should be(Left(UnclosedInvertedSection("name")))
    }

    it("does not render if the key is in the context and is a non-empty iterable") {
      InvertedSection.process(
        "name",
        Map("name" -> List("hello")),
        "section{{/name}}remaining",
        unusedRender) should be(Right("", "remaining"))
    }

    it("renders once for the current context if the key is not in the context") {
      val render = (template: String, context: Context) => Right(s"""RENDERED(${context(template)})""")
      InvertedSection.process(
        "name",
        Map("template" -> "Jim"),
        "template{{/name}}remaining",
        render) should be(Right("RENDERED(Jim)", "remaining"))
    }

    it("renders once for the current context if the key is in the context and is an empty iterable") {
      val render = (template: String, context: Context) => Right(s"""RENDERED(${context(template)})""")
      InvertedSection.process(
        "name",
        Map("name" -> List(), "template" -> "Jim"),
        "template{{/name}}remaining",
        render) should be(Right("RENDERED(Jim)", "remaining"))
    }

    it("renders once for the current context if the key is in the context and is false") {
      val render = (template: String, context: Context) => Right(s"""RENDERED(${context(template)})""")
      InvertedSection.process(
        "name",
        Map("name" -> false, "template" -> "Jim"),
        "template{{/name}}remaining",
        render) should be(Right("RENDERED(Jim)", "remaining"))
    }
  }

  private val unusedRender = (template: String, context: Context) => Right(template)
}
