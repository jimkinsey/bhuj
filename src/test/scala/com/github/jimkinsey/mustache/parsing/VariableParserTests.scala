package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.components.Variable
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class VariableParserTests extends FunSpec {

  describe("A variable parser") {

    it("does not match if the name contains inappropriate chars") {
      Set("*ABC", "+abc", "1bc").foreach { name => VariableParser.pattern.findPrefixMatchOf(name) should not be defined }
    }

    it("matches if the first char is not a letter or underscore") {
      Set("ABC", "abc", "_bc").foreach { name => VariableParser.pattern.findPrefixMatchOf(name) should be(defined) }
    }

    it("returns a variable component with the name") {
      VariableParser.parsed("foo") should be(Right(Variable("foo")))
    }
    
  }

}
