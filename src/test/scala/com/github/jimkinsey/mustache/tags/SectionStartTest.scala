package com.github.jimkinsey.mustache.tags

import com.github.jimkinsey.mustache.Renderer.{Result, Context}
import com.github.jimkinsey.mustache.tags.SectionStart.{Render, Lambda, UnclosedSection}
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class SectionStartTest extends FunSpec {

  describe("A section start tag") {

    it("does not match when the tag does not start with #") {
      SectionStart.pattern.findFirstIn("not a section") should not be defined
    }

    it("captures the section name") {
      SectionStart.pattern.findFirstMatchIn("#section").get.group(1) should be("section")
    }

    it("returns an UnclosedSection failure when the closing tag is not found") {
      SectionStart.process("section", Map.empty, "not closed!!!", render) should be(Left(UnclosedSection("section")))
    }

    it("replaces the whole section with nothing if the key is not in the context") {
      SectionStart.process("section", Map.empty, "template{{/section}}", render) should be(Right("", ""))
    }

    it("returns the template after the section close tag") {
      SectionStart.process("section", Map.empty, "{{/section}} after", render) should be(Right("", " after"))
    }

    it("does not render if the named value in the context is false") {
      SectionStart.process("section", Map("section" -> false), "true!{{/section}}", render) should be(Right("", ""))
    }

    it("does not render if the named value in the context is an empty iterable") {
      SectionStart.process("section", Map("section" -> List.empty), "non-empty!{{/section}}", render) should be(Right("", ""))
    }

    it("returns the failure if the named value is a lambda which fails") {
      val failingLambda: Lambda = (_, _) => Left(UnclosedSection("lol"))
      SectionStart.process("section", Map("section" -> failingLambda), "lambda{{/section}}", render) should be(Left(UnclosedSection("lol")))
    }

    it("returns the failure if the named value is a non-false value which fails to render") {
      val failingRender: Render = (_, _) => Left(UnclosedSection("lol"))
      SectionStart.process("section", Map("section" -> Map("a" -> 1)), "{{a}}{{/section}}", failingRender) should be(Left(UnclosedSection("lol")))
    }

    it("renders the section once using the value as a context if it is a map") {
      val render: Render = (template, context) => Right(context(template).toString)
      SectionStart.process("section", Map("section" -> Map("a" -> 1)), "a{{/section}}", render) should be(Right("1", ""))
    }

    it("renders the section once in the current context if it is true") {
      val render: Render = (template, context) => Right(context(template).toString)
      SectionStart.process("section", Map("section" -> true), "section{{/section}}", render) should be(Right("true", ""))
    }

    it("renders the section for each item in a non-empty iterable with the item as the context") {
      val render: Render = (template, context) => Right(context(template).toString)
      SectionStart.process(
        name = "section",
        context = Map("section" -> List(Map("a" -> 1), Map("a" -> 2))),
        postTagTemplate = "a{{/section}}",
        render) should be(Right("12", ""))
    }

    it("renders the section once for a lambda, passing the template and render function in") {
      val render: Render = (template, context) => Right(context(template).toString)
      val lambda: Lambda = (template, render) => Right(s"LAMBDA'D: ${render(template).right.get}")
      SectionStart.process(
        name = "section",
        context = Map("section" -> lambda, "a" -> 42),
        postTagTemplate = "a{{/section}}",
        render
      ) should be(Right("LAMBDA'D: 42", ""))
    }

  }

  private val render: (String, Context) => Result = (a, b) => ???
}
