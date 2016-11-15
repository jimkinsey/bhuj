package bhuj.formatting

import bhuj.model.{SetDelimiters, Template, Variable}
import bhuj.parsing.Delimiters
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class FormatterTests extends FunSpec {

  describe("Formatting") {

    describe("A template") {

      it("concatenates the result of formatting each component") {
        val component1 = Variable("component1")
        val component2 = Variable("component2")
        formatter.source(Template(component1, component2)) should be("{{component1}}{{component2}}")
      }

      it("obeys the set delimiters tag when formatting") {
        val a = Variable("a")
        val b = Variable("b")
        val setDelimiters = SetDelimiters(Delimiters("(:", ":)"))
        formatter.source(Template(a, setDelimiters, b)) should be("{{a}}{{=(: :)=}}(:b:)")
      }

    }

  }

  private lazy val formatter = new Formatter()

}
