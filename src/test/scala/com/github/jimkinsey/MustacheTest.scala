package com.github.jimkinsey

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class MustacheTest extends FunSpec {

  describe("Mustache") {

    it("leaves a string containing no tags untouched") {
      new Mustache().render("No tags") should be("No tags")
    }

    it("leaves an empty string untouched") {
      new Mustache().render("") should be("")
    }

    describe("a variable tag") {

      it("is replaced by an empty string when the key is not in the context") {
        new Mustache().render("Hello {{name}}", Map.empty) should be("Hello ")
      }

      it("is replaced by the value from the context when present") {
        new Mustache().render("Hello {{name}}", Map("name" -> "Chris")) should be("Hello Chris")
      }

      it("works with multiple variables") {
        new Mustache().render("Hi {{first}} {{last}}", Map("first" -> "John", "last" -> "Smith")) should be("Hi John Smith")
      }

      it("escapes for HTML by default") {
        new Mustache().render("{{html}}", Map("html" -> """<blink>"&'</blink>""")) should be("&lt;blink&gt;&quot;&amp;&#39;&lt;/blink&gt;")
      }

    }
  }

}