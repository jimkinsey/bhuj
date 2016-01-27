package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.CanContextualiseCaseClass.NotACaseClass
import com.github.jimkinsey.mustache.tags.SectionStart.Lambda
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

    it("can contextualise a String field") {
      case class Named(name: String)
      new CanContextualiseCaseClass().context(Named("Charley")) should be(Right(Map("name" -> "Charley")))
    }

    it("can contextualise a Boolean field") {
      case class Flagged(awesome: Boolean)
      new CanContextualiseCaseClass().context(Flagged(true)) should be(Right(Map("awesome" -> true)))
    }

    it("can contextualise a Mustache-compatible lambda field") {
      case class Ram(lambda: Lambda)
      val lambda: Lambda = (str, render) => Right("Lambdad!")
      new CanContextualiseCaseClass().context(Ram(lambda)) should be(Right(Map("lambda" -> lambda)))
    }

  }

}
