package com.github.jimkinsey.mustache.context

import com.github.jimkinsey.mustache.Context
import com.github.jimkinsey.mustache.context.CanContextualise.Failure
import com.github.jimkinsey.mustache.context.CanContextualiseCaseClass.ConversionFailure

object CanContextualiseCaseClass {
  case class ConversionFailure(failure: CaseClassConverter.Failure) extends Failure
}

class CanContextualiseCaseClass(converter: CaseClassConverter) extends CanContextualise[Product] {
  def context(obj: Product): Either[Failure, Context] = {
    converter.map(obj).left.map(ConversionFailure.apply)
  }
}
