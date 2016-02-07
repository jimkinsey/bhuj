package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.TemplateParser.{InvalidParserPattern, TagParseFailure, UnrecognisedTag}
import com.github.jimkinsey.mustache.components.Text
import com.github.jimkinsey.mustache.rendering.Renderer.Context
import com.github.jimkinsey.mustache.rendering.{Component, Template}
import org.scalatest.FunSpec
import org.scalatest.Matchers._

import scala.util.matching.Regex

class TemplateParserTests extends FunSpec {

  describe("A template parser") {

    it("returns an empty template for an empty string") {
      parser.parse("") should be(Right(Template()))
    }

    it("returns a template with a text component for a string with no tags") {
      parser.parse("No tags here!") should be(Right(Template(Text("No tags here!"))))
    }

    it("fails if no tag parser is registered which matches the tag content") {
      parser.parse("what {{bar}}") should be(Left(UnrecognisedTag(5)))
    }

    it("fails if the found tag parser fails") {
      parser.parse("this will {{fail}}") should be(Left(TagParseFailure(10, failure)))
    }

    it("appends the result of processing the tag to the template") {
      parser.parse("{{succeed}}") should be(Right(Template(NamedComponent("succeed"))))
    }

    it("turns text outside of tags into text components") {
      parser.parse("Text! {{succeed}}") should be(Right(Template(Text("Text! "), NamedComponent("succeed"))))
    }

    it("works for multiple tags") {
      parser.parse("{{succeed}} = {{succeed}}") should be(Right(Template(NamedComponent("succeed"), Text(" = "), NamedComponent("succeed"))))
    }

    it("returns the first failure to parse") {
      parser.parse("{{succeed}}{{fail}}{{fail}}") should be(Left(TagParseFailure(11, failure)))
    }

    it("returns the first unrecognised tag") {
      parser.parse("{{succeed}}{{unknown}}{{fail}}") should be(Left(UnrecognisedTag(11)))
    }

    it("passes the name of the tag to the tag parser") {
      parser.parse("{{succeed}}") should be(Right(Template(NamedComponent("succeed"))))
    }

    it("only matches the name from directly after the start delimiter") {
      parser.parse("{{donotsucceed}}") should be(Left(UnrecognisedTag(0)))
    }

    it("only matches the name up to the end delimiter") {
      parser.parse("{{succeednot}}") should be(Left(UnrecognisedTag(0)))
    }

    it("matches if chars from the end of the name are in the start of the end delimiter") {
      parser.parse("{{{succeed}}}") should be(Right(Template(NamedComponent("TRIPLE:succeed"))))
    }

    it("fails if the parser does not have any groups in the regex pattern") {
      parser.parse("{{invalid}}") should be(Left(InvalidParserPattern(invalid.pattern)))
    }

  }

  private val failure = new TagParser.Failure {}

  private case class NamedComponent(name: String) extends Component {
    def rendered(context: Context) = ???
  }

  private val component = new Component {
    def rendered(context: Context) = ???
  }

  private val succeeding = new TagParser {
    lazy val pattern = "(succeed)".r
    def parsed(name: String) = Right(NamedComponent(name))
  }

  private val failing = new TagParser {
    lazy val pattern = "(fail)".r
    def parsed(name: String) = Left(failure)
  }

  private val triple_braces = new TagParser {
    lazy val pattern = """\{(succeed)\}""".r
    def parsed(name: String) = Right(NamedComponent(s"TRIPLE:$name"))
  }

  private val invalid = new TagParser {
    lazy val pattern: Regex = """invalid""".r
    def parsed(name: String) = ???
  }

  private val parser: TemplateParser = new TemplateParser(tagParsers = Seq(succeeding, failing, triple_braces, invalid))

}
