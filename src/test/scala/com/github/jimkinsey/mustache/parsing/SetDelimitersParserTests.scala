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

  }

}
