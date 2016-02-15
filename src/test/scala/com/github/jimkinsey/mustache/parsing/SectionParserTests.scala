package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.components.{Text, Template}
import com.github.jimkinsey.mustache.parsing.ContainerTagComponentParser.UnclosedTag
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class SectionParserTests extends FunSpec {
  private implicit val parserConfig = ParserConfig(t => Right(Template(Text(t))), (_,_) => ???)

  describe("A section parser") {

    it("returns no section if the tag does not start with #") {
      SectionParser.parseResult("{{not a section}}") should be(Right(None))
    }

    it("returns no section if the string is empty") {
      SectionParser.parseResult("") should be(Right(None))
    }

    it("fails if there is no closing tag") {
      SectionParser.parseResult("{{#section}}unclosed") should be(Left(UnclosedTag))
    }

    it("fails if there is a closing tag whose name does not match") {
      SectionParser.parseResult("{{#section}}wrong{{/noitces}}") should be(Left(UnclosedTag))
    }

    it("returns a section with the correct name") {
      SectionParser.parseResult("{{#section}}{{/section}}").right.get.get.component.name should be("section")
    }

    it("returns the remainder after the closing section tag") {
      SectionParser.parseResult("{{#a}}{{/a}}bcd").right.get.get.remainder should be("bcd")
    }

    it("returns a section containing the content as a template") {
      SectionParser.parseResult("{{#t}}inner{{/t}}").right.get.get.component.template should be(Template(Text("inner")))
    }

    it("propagates a failure to parse the inner template") {
      implicit val parserConfig = ParserConfig(_ => Left("BOOM"), (_,_) => ???)
      SectionParser.parseResult("{{#t}}fail{{/t}}") should be(Left("BOOM"))
    }

    it("allows for a nested section with the same key") {
      SectionParser.parseResult("{{#t}}a{{#t}}b{{/t}}c{{/t}}").right.get.get.component.template should be(Template(Text("a{{#t}}b{{/t}}c")))
    }

    it("accounts for nested sections with alternative prefixes") {
      SectionParser.parseResult("{{#t}}a{{^t}}b{{/t}}c{{/t}}").right.get.get.component.template should be(Template(Text("a{{^t}}b{{/t}}c")))
    }

  }

}
