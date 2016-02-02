package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.components.{UnescapedVariable, Variable}
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class UnescapedVariableParserTests extends FunSpec {

   describe("An unescaped variable parser") {

     it("does not match if the first char of the name is not a letter or underscore") {
       Set("{*ABC}", "{+abc}", "{1bc}").foreach { name =>
         UnescapedVariableParser.pattern.findFirstMatchIn(name) should not be defined
       }
     }

     it("does not match if the remaining chars of the name are not a letter, undercore, number or plus sign") {
       Set("{A*}", "{a{}", "{a`}").foreach { name =>
         UnescapedVariableParser.pattern.findFirstMatchIn(name) should not be defined
       }
     }

     it("matches if the first char of the name is not a letter or underscore") {
       Set("{ABC}", "{abc}", "{_bc}").foreach { name =>
         UnescapedVariableParser.pattern.findFirstMatchIn(name) should be(defined)
       }
     }

     it("returns a variable component with the name") {
       UnescapedVariableParser.parsed("foo") should be(Right(UnescapedVariable("foo")))
     }

   }

 }
