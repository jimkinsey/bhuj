package bhuj.parsing

import bhuj.components.Text
import bhuj.{Failure, Template, UnclosedTag}
import org.scalatest.EitherValues._
import org.scalatest.FunSpec
import org.scalatest.Matchers._
import org.scalatest.mock.MockitoSugar._

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
      SectionParser.parseResult("{{#section}}unclosed") should be(Left(UnclosedTag("section")))
    }

    it("fails if there is a closing tag whose name does not match") {
      SectionParser.parseResult("{{#section}}wrong{{/noitces}}") should be(Left(UnclosedTag("section")))
    }

    it("returns a section with the correct name") {
      SectionParser.parseResult("{{#section}}{{/section}}").right.get.get.component.name should be("section")
    }

    it("returns the remainder after the closing section tag") {
      SectionParser.parseResult("{{#a}}{{/a}}bcd").right.get.get.remainder should be("bcd")
    }

    it("returns a section containing the content as a template") {
      SectionParser.parseResult("{{#t}}inner{{/t}}").right.get.get.component.template should have('source ("inner"))
    }

    it("propagates a failure to parse the inner template") {
      val failure = mock[Failure]
      implicit val parserConfig = ParserConfig(_ => Left(failure), (_,_) => ???)
      SectionParser.parseResult("{{#t}}fail{{/t}}") should be(Left(failure))
    }

    it("allows for a nested section with the same key") {
      SectionParser.parseResult("{{#t}}a{{#t}}b{{/t}}c{{/t}}").right.value.get.component.template should have('source ("a{{#t}}b{{/t}}c"))
    }

    it("accounts for nested sections with alternative prefixes") {
      SectionParser.parseResult("{{#t}}a{{^t}}b{{/t}}c{{/t}}").right.value.get.component.template should have('source ("a{{^t}}b{{/t}}c"))
    }

    it("accounts for nested tags with the same key") {
      SectionParser.parseResult("{{#t}}a{{{t}}}b{{/t}}").right.value.get.component.template should have('source ("a{{{t}}}b"))
    }

  }

}
