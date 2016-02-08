package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache._
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class UnescapedVariableTests extends FunSpec {
  implicit val global: Context = Map.empty

  describe("An unescaped variable") {
    it("returns an empty string if the key is not in the context") {
      UnescapedVariable("name").rendered(Map("age" -> 37)) should be(Right(""))
    }

    it("returns the value from the context when present") {
      UnescapedVariable("name").rendered(Map("name" -> "Jim")) should be(Right("Jim"))
    }

    it("uses the value from the global context when not in the local") {
      implicit val global: Context = Map("name" -> "Jim")
      UnescapedVariable("name").rendered(Map.empty) should be(Right("Jim"))
    }

    it("overrides the value from the global context with the local") {
      implicit val global: Context = Map("name" -> "James")
      UnescapedVariable("name").rendered(Map("name" -> "Jim")) should be(Right("Jim"))
    }

    it("converts the value to a string when it is not") {
      UnescapedVariable("age").rendered(Map("age" -> 37)) should be(Right("37"))
    }

    it("does not escape the value for HTML") {
      UnescapedVariable("fragment").rendered(Map("fragment" -> """<blink>"&'</blink>""")) should be(Right("""<blink>"&'</blink>"""))
    }
  }

}
