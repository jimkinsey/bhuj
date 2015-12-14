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

  }

}