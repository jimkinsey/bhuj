package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.CaseClassConverter.{NotACaseClass, Failure}
import com.github.jimkinsey.mustache.tags.SectionStart._
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class CaseClassConverterTests extends FunSpec {
  import converter.map

  describe("A Case Class Converter") {

    it("cannot convert a Product which is not a case class") {
      val notACaseClass = new Product {
        override def productElement(n: Int): Any = 42
        override def productArity: Int = 3
        override def canEqual(that: Any): Boolean = false
      }
      map(notACaseClass) should be(Left(NotACaseClass(notACaseClass)))
    }
    
    it("strips out the reference to the outer class when the case class is inner") {
      case class Inner(n: Int)
      map(Inner(7)).right.get.keySet should not contain "$outer"
    }

    it("can convert a case class with an Int field") {
      case class Numbered(n: Int)
      map(Numbered(42)) should be(Right(Map("n" -> 42)))
    }

    it("can convert a String field") {
      case class Named(name: String)
      map(Named("Charley")) should be(Right(Map("name" -> "Charley")))
    }

    it("can convert a Boolean field") {
      case class Flagged(awesome: Boolean)
      map(Flagged(awesome = true)) should be(Right(Map("awesome" -> true)))
    }

    it("can convert a Mustache-compatible lambda field") {
      case class Ram(lambda: Lambda)
      val lambda: Lambda = (str, render) => Right("Lambdad!")
      map(Ram(lambda)) should be(Right(Map("lambda" -> lambda)))
    }

    it("recursively converts a case class") {
      case class PostCode(areaCode: String, code: String)
      case class Address(postCode: PostCode)
      map(
        Address(
          PostCode("SE10", "8HR")
        )
      ) should be(Right(
        Map(
          "postCode" -> Map("areaCode" -> "SE10", "code" -> "8HR")
        )
      ))
    }

    it("recursively converts an iterable") {
      case class Thing(n: Int)
      case class Container(things: Seq[Thing])
      map(
        Container(Seq(Thing(1), Thing(2)))
      ) should be(Right(
        Map("things" -> Seq(Map("n" -> 1), Map("n" -> 2)))
      ))
    }

    it("recursively converts a map") {
      case class Navigator(map: Map[Int, Int])
      map(
        Navigator(Map(1 -> 2))
      ) should be(Right(Map("map" -> Map("1" -> 2))))
    }

  }

  private val converter: CaseClassConverter = new CaseClassConverter()

}
