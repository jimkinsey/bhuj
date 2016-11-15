package bhuj.parsing

import bhuj.Failure
import bhuj.model._
import org.mockito.Mockito.when
import org.scalatest.EitherValues._
import org.scalatest.FunSpec
import org.scalatest.Matchers._
import org.scalatest.mock.MockitoSugar.mock

class TemplateParserTests extends FunSpec {
  import org.mockito.Matchers.{any, eq => equalTo}
  private implicit val parserConfig = ParserConfig(_ => ???, (_,_) => ???)

  describe("A template parser") {

    it("returns an empty template if the string is empty") {
      new TemplateParser().template("") should be(Right(Template()))
    }

    it("propagates the failure of one of its component parsers") {
      val failure = mock[Failure]
      val failing = mock[ComponentParser[Component]]
      when(failing.parseResult(equalTo("abc"))(any())).thenReturn(Left(failure))
      new TemplateParser(failing).template("abc") should be(Left(failure))
    }

    it("fails fast") {
      val failure = mock[Failure]
      val failing1 = mock[ComponentParser[Component]]
      when(failing1.parseResult(equalTo("abc"))(any())).thenReturn(Left(failure))
      val failing2 = mock[ComponentParser[Component]]
      when(failing2.parseResult(equalTo("abc"))(any())).thenReturn(Left(failure))
      new TemplateParser(failing1, failing2).template("abc") should be(Left(failure))
    }

    it("returns an empty template if no parser matches") {
      val abcParser = mock[ComponentParser[Component]]
      when(abcParser.parseResult(equalTo("xyz"))(any())).thenReturn(Right(None))
      new TemplateParser(abcParser).template("xyz") should be(Right(Template()))
    }

    it("returns a template with the initial delimiters from the parser config") {
      val newParserConfig = parserConfig.copy(delimiters = Delimiters("A", "B"))
      new TemplateParser().template("")(newParserConfig).right.value.initialDelimiters should be(Delimiters("A", "B"))
    }

    it("returns a template containing the component from the successful parse result") {
      val xyzParser = mock[ComponentParser[Component]]
      val xyzComponent = mock[Component]
      when(xyzParser.parseResult(equalTo("xyz"))(any())).thenReturn(Right(Some(ParseResult(xyzComponent, ""))))
      new TemplateParser(xyzParser).template("xyz") should be(Right(Template(xyzComponent)))
    }

    it("returns a template based on all matching parsers") {
      val xyzParser = mock[ComponentParser[Component]]
      val xyzComponent = mock[Component]
      when(xyzParser.parseResult(equalTo("xyzabc"))(any())).thenReturn(Right(Some(ParseResult(xyzComponent, "abc"))))
      val abcParser = mock[ComponentParser[Component]]
      val abcComponent = mock[Component]
      when(abcParser.parseResult(equalTo("abc"))(any())).thenReturn(Right(Some(ParseResult(abcComponent, ""))))
      new TemplateParser(xyzParser, abcParser).template("xyzabc") should be(Right(Template(xyzComponent, abcComponent)))
    }

    it("uses the parser config provided by the component when it is a directive") {
      new TemplateParser(SetDelimitersParser, VariableParser).template("{{=~: :~=}}~:foo:~") should be(Right(
        Template(SetDelimiters(Delimiters("~:", ":~")), Variable("foo"))
      ))
    }

  }

}
