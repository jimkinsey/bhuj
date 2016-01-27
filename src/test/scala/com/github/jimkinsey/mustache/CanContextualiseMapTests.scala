package com.github.jimkinsey.mustache

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class CanContextualiseMapTests extends FunSpec {

  describe("Contextualising a map") {

    it("returns an empty map if the map is empty") {
      new CanContextualiseMap().context(Map[String,Any]()) should be(Right(Map.empty))
    }

    it("returns the map if non-empty") {
      new CanContextualiseMap().context(Map("x" -> 42)) should be(Right(Map("x" -> 42)))
    }

  }

}
