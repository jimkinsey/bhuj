package com.github.jimkinsey.mustache.tags

import com.github.jimkinsey.mustache.rendering.Renderer
import com.github.jimkinsey.mustache.tags.Partial.PartialNotFound
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class PartialTest extends FunSpec {

  describe("A partial tag") {

    it("starts with a greater-than sign ('>')") {
      new Partial().pattern.findFirstIn("> some-partial") should be(defined)
    }

    it("it captures the name of the partial, discarding whitespace") {
      new Partial().pattern.findFirstMatchIn("> some-partial ").get.group(1) should be("some-partial")
    }

    it("does not match when it does not start with a greater-than sign") {
      new Partial().pattern.findFirstIn("not-a-partial") should not be defined
    }

    it("returns a PartialNotFound failure when the partial with the name is not available") {
      new Partial().process("not-found", Map.empty, "", (a,b) => ???) should be(Left(PartialNotFound("not-found")))
    }

    it("returns the result of rendering the partial in the current context when it is available") {
      val render = (template: String, context: Renderer.Context) => Right(s"RENDERED(${context(template)})")
      new Partial(Map("partial" -> "a"))
        .process("partial", Map("a" -> 1), "remaining", render) should be(Right("RENDERED(1)" ->  "remaining"))
    }

    it("returns a failure if the rendering failed") {
      val render = (template: String, context: Renderer.Context) => Left(PartialNotFound(template))
      new Partial(Map("partial" -> "a"))
        .process("partial", Map("a" -> 1), "remaining", render) should be(Left(PartialNotFound("a")))
    }

  }

}
