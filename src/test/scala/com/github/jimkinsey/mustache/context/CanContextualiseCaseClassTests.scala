package com.github.jimkinsey.mustache.context

import com.github.jimkinsey.mustache.context.CanContextualiseCaseClass.ConversionFailure
import com.github.jimkinsey.mustache.context.CaseClassConverter.GeneralFailure
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.FunSpec
import org.scalatest.Matchers._
import org.scalatest.mock.MockitoSugar.mock

class CanContextualiseCaseClassTests extends FunSpec {
  private val converter = mock[CaseClassConverter]
  private val contextualiser = new CanContextualiseCaseClass(converter)
  import contextualiser._

  describe("CanContextualiseCaseClass") {

    it("delegates to the converter") {
      case class House(number: Int)
      when(converter.map(any())).thenReturn(Right(Map("hello" -> "world")))
      context(House(11)) should be(Right(Map("hello" -> "world")))
    }

    it("wraps a conversion error in a contextualisation error") {
      case class Cat(name: String)
      when(converter.map(any())).thenReturn(Left(GeneralFailure("#fail")))
      context(Cat("Eowyn")) should be(Left(ConversionFailure(GeneralFailure("#fail"))))
    }

  }

}
