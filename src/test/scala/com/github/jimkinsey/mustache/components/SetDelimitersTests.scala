package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.Context
import com.github.jimkinsey.mustache.parsing.{Delimiters, ParserConfig}
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class SetDelimitersTests extends FunSpec {
  implicit val global: Context = Map.empty

  describe("A set delimiters directive") {

    it("renders to an empty string") {
      new SetDelimiters(Delimiters("[[", "]]")).rendered(Map.empty) should be(Right(""))
    }

    it("modifies the incoming parser config's delimiters") {
      implicit val config = ParserConfig(_ => ???, (_,_) => ???, Delimiters("{{", "}}"))
      new SetDelimiters(Delimiters("[[", "]]")).modified(config).delimiters should be(Delimiters("[[", "]]"))
    }

  }

}
