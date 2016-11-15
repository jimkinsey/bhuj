package bhuj.context

import bhuj.context.CanContextualiseCaseClass.ConversionFailure
import bhuj.context.CaseClassConverter.NotACaseClass
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class CanContextualiseCaseClassTests extends FunSpec {
  private val converter = new CaseClassConverter
  private val contextualiser = new CanContextualiseCaseClass(converter)
  import contextualiser._

  describe("CanContextualiseCaseClass") {

    it("delegates to the converter") {
      case class House(number: Int)
      context(House(11)) should be(Right(Map("number" -> 11)))
    }

    it("wraps a conversion error in a contextualisation error") {
      case class Cat(name: String) {
        val fail = "Can't handle non constructor fields"
      }
      context(Cat("Eowyn")) should be(Left(ConversionFailure(NotACaseClass(Cat("Eowyn")))))
    }

  }

}
