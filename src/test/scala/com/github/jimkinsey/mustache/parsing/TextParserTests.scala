package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.components.Text
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class TextParserTests extends FunSpec {
  private implicit val parserConfig = ParserConfig(_ => ???, (_,_) => ???)

  describe("A text node parser") {

    it("returns no text node if the string is empty") {
      TextParser.parseResult("") should be(Right(None))
    }

    it("returns no text node if the string starts with a tag delimiter") {
      TextParser.parseResult("{{hello") should be(Right(None))
    }

    it("returns a text node for the entire template when the template contains no tag delimiter") {
      TextParser.parseResult("hello").right.get.get.component should be(Text("hello"))
    }

    it("returns an empty string for the remainder when there no tag delimiter") {
      TextParser.parseResult("hello").right.get.get.remainder should be("")
    }

    it("returns a text node for the text up to the tag")             {
      TextParser.parseResult("hello {{name}}").right.get.get.component should be(Text("hello "))
    }

    it("returns the string from the tag delimiter onwards as a remainder") {
      TextParser.parseResult("hello {{name}}").right.get.get.remainder should be("{{name}}")
    }

  }

}
