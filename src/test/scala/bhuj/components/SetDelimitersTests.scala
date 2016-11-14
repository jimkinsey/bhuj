package bhuj.components

import bhuj.parsing.{Delimiters, ParserConfig}
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class SetDelimitersTests extends FunSpec {

  describe("A set delimiters directive") {

    it("modifies the incoming parser config's delimiters") {
      implicit val config = ParserConfig(_ => ???, (_,_) => ???, Delimiters("{{", "}}"))
      SetDelimiters(Delimiters("[[", "]]")).modified(config) should be(config.copy(delimiters = Delimiters("[[", "]]")))
    }

  }

}
