package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.components.SetDelimiters
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class SetDelimitersParserTests extends FunSpec {
  implicit val parserConfig = ParserConfig(_ => ???, (_,_) => ???, Delimiters("{{", "}}"))

  describe("A set delimiters parser") {

    it("it matches if the tag matches `START=NEW_START NEW_END=END`") {
      SetDelimitersParser.parseResult("{{=<% %>=}}") should be(Right(Some(ParseResult(SetDelimiters(Delimiters("<%", "%>")), ""))))
    }

    it("returns a failure if the start delimiter contains whitespace") {
      SetDelimitersParser.parseResult("{{= x x=}}") should be(Left(InvalidDelimiters(" x", "x")))
    }

    it("returns a failure if the start delimiter contains an equals sign") {
      SetDelimitersParser.parseResult("{{=x= x=}}") should be(Left(InvalidDelimiters("x=", "x")))
    }

    it("returns a failure if the end delimiter contains an equals sign") {
      SetDelimitersParser.parseResult("{{=x =x=}}") should be(Left(InvalidDelimiters("x", "=x")))
    }

    it("returns a failure if the end delimiter contains whitespace") {
      SetDelimitersParser.parseResult("{{=x x =}}") should be(Left(InvalidDelimiters("x", "x ")))
    }

  }

}
