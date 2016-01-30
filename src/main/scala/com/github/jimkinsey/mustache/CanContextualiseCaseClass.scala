package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.CanContextualise.Failure
import com.github.jimkinsey.mustache.CanContextualiseCaseClass.ConversionFailure
import com.github.jimkinsey.mustache.Renderer.Context

object CanContextualiseCaseClass {
  case class ConversionFailure(failure: CaseClassConverter.Failure) extends Failure
}

class CanContextualiseCaseClass(converter: CaseClassConverter) extends CanContextualise[Product] {
  def context(obj: Product): Either[Failure, Context] = {
    converter.map(obj).left.map(ConversionFailure.apply)
  }
}
