package com.github.jimkinsey.mustache

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class MapContextualiserTests extends FunSpec {

  describe("A map contextualiser") {

    it("returns an empty map if the map is empty") {
      new MapContextualiser().context(Map[String,Any]()) should be(Right(Map.empty))
    }

    it("returns the map if non-empty") {
      new MapContextualiser().context(Map("x" -> 42)) should be(Right(Map("x" -> 42)))
    }

  }

}
