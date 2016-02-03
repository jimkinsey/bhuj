package com.github.jimkinsey.mustache.tags

import com.github.jimkinsey.mustache.rendering.Renderer
import Renderer.Context
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class VariableTest extends FunSpec {

  describe("A variable tag") {

    it("does not match sections") {
      Variable.pattern.findFirstIn("#section") should not be defined
    }

    it("does not match inverted sections") {
      Variable.pattern.findFirstIn("^invertedsection") should not be defined
    }

    it("does not match comments") {
      Variable.pattern.findFirstIn("! comment ") should not be defined
    }

    it("does not match partials") {
      Variable.pattern.findFirstIn("> partial") should not be defined
    }

    it("captures the key") {
      Variable.pattern.findFirstMatchIn("variable").get.group(1) should be("variable")
    }

    it("is replaced with no content when the key is not in the context") {
      Variable.process("x", Map.empty, "", render) should be(Right("" ->  ""))
    }

    it("is replaced by the value from the context") {
      Variable.process("x", Map("x" -> 1), "", render) should be(Right("1" ->  ""))
    }

    it("is escaped for HTML by default") {
      Variable.process("html", Map("html" -> """<blink>"&'</blink>"""), "", render) should be(Right("&lt;blink&gt;&quot;&amp;&#39;&lt;/blink&gt;" ->  ""))
    }

    it("passes on the post-tag-template") {
      Variable.process("a", Map.empty, "template", render) should be(Right("" ->  "template"))
    }

  }

  private val render: (String, Context) => Nothing = (a, b) => ???
}
