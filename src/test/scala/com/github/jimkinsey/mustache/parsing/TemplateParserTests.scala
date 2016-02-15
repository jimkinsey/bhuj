package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.components._
import com.github.jimkinsey.mustache.parsing.ContainerTagComponentParser.UnclosedTag
import org.mockito.Mockito.when
import org.scalatest.Matchers._
import org.scalatest.mock.MockitoSugar.mock
import org.scalatest.{FunSpec, Tag}

class TemplateParserTests extends FunSpec {
  import org.mockito.Matchers.{any, eq => equalTo}
  private implicit val parserConfig = ParserConfig(_ => ???, (_,_) => ???)

  describe("A template parser") {

    it("returns an empty template if the string is empty") {
      new TemplateParser().template("") should be(Right(Template()))
    }

    it("propagates the failure of one of its component parsers") {
      val failure = "BOOM!"
      val failing = mock[ComponentParser[Component]]
      when(failing.parseResult(equalTo("abc"))(any())).thenReturn(Left(failure))
      new TemplateParser(failing).template("abc") should be(Left(failure))
    }

    it("fails fast") {
      val failing1 = mock[ComponentParser[Component]]
      when(failing1.parseResult(equalTo("abc"))(any())).thenReturn(Left(1))
      val failing2 = mock[ComponentParser[Component]]
      when(failing2.parseResult(equalTo("abc"))(any())).thenReturn(Left(2))
      new TemplateParser(failing1, failing2).template("abc") should be(Left(1))
    }

    it("returns an empty template if no parser matches") {
      val abcParser = mock[ComponentParser[Component]]
      when(abcParser.parseResult(equalTo("xyz"))(any())).thenReturn(Right(None))
      new TemplateParser(abcParser).template("xyz") should be(Right(Template()))
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

  }

}
