package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.Context
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class PartialParserTests extends FunSpec {
  private lazy implicit val parseConfig = ParserConfig(_ => ???, (_,_) => ???)

  describe("A partial parser") {

    it("does not match if the tag does not start with a greater-than sign") {
      new PartialParser((_,_) => ???).parseResult("{{< less-than}}") should be(Right(None))
    }

    it("does match if the tag starts with a greater-than sign") {
      new PartialParser((_,_) => ???).parseResult("{{> greater-than}}").right.get should be(defined)
    }

    it("trims whitespace when extracting the partial name") {
      new PartialParser((_,_) => ???).parseResult("{{> spaced }}").right.get.get.component.name should be("spaced")
    }

    it("sets up the partial with the named-template-rendering function") {
      val render: (String, Context) => Either[Any,String] = (_,_) => ???
      new PartialParser(render).parseResult("{{> partial}}").right.get.get.component.render should be(render)
    }
  }
}
