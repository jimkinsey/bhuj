package bhuj.parsing

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class PartialParserTests extends FunSpec {
  private lazy implicit val parseConfig = ParserConfig(parsed = _ => ???)

  describe("A partial parser") {

    it("does not match if the tag does not start with a greater-than sign") {
      PartialParser.parseResult("{{< less-than}}") should be(Right(None))
    }

    it("does match if the tag starts with a greater-than sign") {
      PartialParser.parseResult("{{> greater-than}}").right.get should be(defined)
    }

    it("trims whitespace when extracting the partial name") {
      PartialParser.parseResult("{{> spaced }}").right.get.get.component.name should be("spaced")
    }

  }
}
