package bhuj

import bhuj.components.{SetDelimiters, Variable}
import bhuj.parsing.Delimiters
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class TemplateTests extends FunSpec {

  describe("A template") {

    it("formats by calling formatted on each component") {
      val component1 = Variable("component1")
      val component2 = Variable("component2")
      Template(component1, component2).formatted(Delimiters("{{", "}}")) should be("{{component1}}{{component2}}")
    }

    it("obeys the set delimiters tag when formatting") {
      val a = Variable("a")
      val b = Variable("b")
      val setDelimiters = SetDelimiters(Delimiters("(:", ":)"))
      Template(a, setDelimiters, b).source should be("{{a}}{{=(: :)=}}(:b:)")
    }

  }

}
