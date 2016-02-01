package com.github.jimkinsey.mustache.components

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class VariableTests extends FunSpec {

  describe("A variable component") {

    it("returns an empty string if the key is not in the context") {
      Variable("name").rendered(Map("age" -> 37)) should be(Right(""))
    }

    it("returns the value from the context when present") {
      Variable("name").rendered(Map("name" -> "Jim")) should be(Right("Jim"))
    }

    it("converts the value to a string when it is not") {
      Variable("age").rendered(Map("age" -> 37)) should be(Right("37"))
    }

    it("escapes the value for HTML") {
      Variable("fragment").rendered(Map("fragment" -> """<blink>"&'</blink>""")) should be(Right("&lt;blink&gt;&quot;&amp;&#39;&lt;/blink&gt;"))
    }

  }

}
