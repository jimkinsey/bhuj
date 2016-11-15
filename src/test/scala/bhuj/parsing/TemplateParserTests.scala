package bhuj.parsing

import bhuj.UnclosedTag
import bhuj.model._
import org.scalatest.EitherValues._
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class TemplateParserTests extends FunSpec {
  private implicit val parserConfig = ParserConfig(_ => ???)

  describe("A template parser") {

    it("returns an empty template if the string is empty") {
      new TemplateParser().template("") should be(Right(Template()))
    }

    it("fails fast") {
      new TemplateParser(SectionParser).template("{{#b}}bbb{{/B}}{{#c}}ccc{{/C}}") should be(Left(UnclosedTag("b")))
    }

    it("returns an empty template if no parser matches") {
      new TemplateParser(VariableParser).template("xyz") should be(Right(Template()))
    }

    it("returns a template with the initial delimiters from the parser config") {
      val newParserConfig = parserConfig.copy(delimiters = Delimiters("A", "B"))
      new TemplateParser().template("")(newParserConfig).right.value.initialDelimiters should be(Delimiters("A", "B"))
    }

    it("returns a template based on all matching parsers") {
      new TemplateParser(VariableParser, TextParser).template("{{foo}}bar") should be(Right(
        Template(Variable("foo"), Text("bar"))
      ))
    }

    it("uses the parser config provided by the component when it is a directive") {
      new TemplateParser(SetDelimitersParser, VariableParser).template("{{=~: :~=}}~:foo:~") should be(Right(
        Template(SetDelimiters(Delimiters("~:", ":~")), Variable("foo"))
      ))
    }

  }

}
