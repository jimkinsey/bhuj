package com.github.jimkinsey

import com.github.jimkinsey.Renderer.{Tag, Result, Context, UnrecognisedTag}
import org.scalatest.FunSpec
import org.scalatest.Matchers._

import scala.util.matching.Regex

class RendererTest extends FunSpec {

  describe("A renderer") {

    it("returns the input string unchanged when it has no tags") {
      new Renderer(tags = Set.empty).render("Template") should be(Right("Template"))
    }

    it("returns an UnrecognisedTag failure when a tag is encountered which does not match the provided tags") {
      new Renderer(tags = Set.empty).render("{{wut}}") should be(Left(UnrecognisedTag("wut")))
    }

    describe("for a matching tag") {

      it("returns the failure if processing failed") {
        val failingTag = new Tag {
          def pattern: Regex = """(.+)""".r
          def process(name: String, context: Context, postTagTemplate: String, render: (String, Context) => Result) =
            (Left(UnrecognisedTag(name)), "")
        }
        new Renderer(tags = Set(failingTag)).render("{{hello}}") should be(Left(UnrecognisedTag("hello")))
      }

      it("returns the failure if rendering the remaining template failed") {
        val lowercaseNameTag = new Tag {
          def pattern: Regex = """([a-z]+)""".r
          def process(name: String, context: Context, postTagTemplate: String, render: (String, Context) => Result) =
            (Right("pass"), " {{FAIL}} ")
        }
        new Renderer(tags = Set(lowercaseNameTag)).render("{{hello}}") should be(Left(UnrecognisedTag("FAIL")))
      }

      it("passes the name based on the first match in the pattern") {
        var passedName: Option[String] = None
        val lowercaseNameTag = new Tag {
          def pattern: Regex = """([a-z]+)""".r
          def process(name: String, context: Context, postTagTemplate: String, render: (String, Context) => Result) = {
            passedName = Some(name)
            (Right(""), "")
          }
        }
        new Renderer(tags = Set(lowercaseNameTag)).render("{{hello}}")
        passedName should be(Some("hello"))
      }

      it("passes in the context") {
        var passedContext: Option[Context] = None
        val lowercaseNameTag = new Tag {
          def pattern: Regex = """([a-z]+)""".r
          def process(name: String, context: Context, postTagTemplate: String, render: (String, Context) => Result) = {
            passedContext = Some(context)
            (Right(""), "")
          }
        }
        new Renderer(tags = Set(lowercaseNameTag)).render("{{hello}}", Map("a" -> 1))
        passedContext should be(Some(Map("a" -> 1)))
      }

      it("passes in the template after the tag") {
        var passedPostTagTemplate: Option[String] = None
        val lowercaseNameTag = new Tag {
          def pattern: Regex = """([a-z]+)""".r
          def process(name: String, context: Context, postTagTemplate: String, render: (String, Context) => Result) = {
            passedPostTagTemplate = Some(postTagTemplate)
            (Right(""), "")
          }
        }
        new Renderer(tags = Set(lowercaseNameTag)).render("before {{hello}} after")
        passedPostTagTemplate should be(Some(" after"))
      }

      it("concatenates the pre-tag template, the successful result and the result of rendering the post-tag template") {
        val lowercaseNameTag = new Tag {
          def pattern: Regex = """([a-z]+)""".r
          def process(name: String, context: Context, postTagTemplate: String, render: (String, Context) => Result) = {
            (Right(s"RENDERED($name)"), postTagTemplate)
          }
        }
        new Renderer(tags = Set(lowercaseNameTag)).render("before {{hello}} {{world}}") should be(Right("before RENDERED(hello) RENDERED(world)"))
      }
    }

  }

}
