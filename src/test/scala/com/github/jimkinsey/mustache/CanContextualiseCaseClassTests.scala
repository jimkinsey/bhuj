package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.CanContextualiseCaseClass.NotACaseClass
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class CanContextualiseCaseClassTests extends FunSpec {

  describe("CanContextualiseCaseClass") {

    it("works by assuming that case classes are Products") {
      new CanContextualiseCaseClass() should be(a[CanContextualise[Product]])
    }

    it("cannot contextualise a Product which is not a case class") {
      val notACaseClass = new Product {
        override def productElement(n: Int): Any = 42
        override def productArity: Int = 3
        override def canEqual(that: Any): Boolean = false
      }
      new CanContextualiseCaseClass().context(notACaseClass) should be(Left(NotACaseClass(notACaseClass)))
    }

    it("strips out the reference to the outer class when the case class is inner") {
      case class Inner(n: Int)
      new CanContextualiseCaseClass().context(Inner(7)).right.get.keySet should not contain "$outer"
    }

    it("can contextualise a case class with an Int field") {
      case class Numbered(n: Int)
      new CanContextualiseCaseClass().context(Numbered(42)) should be(Right(Map("n" -> 42)))
    }

  }

}
