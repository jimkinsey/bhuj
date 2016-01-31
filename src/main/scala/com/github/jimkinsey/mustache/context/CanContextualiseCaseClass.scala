package com.github.jimkinsey.mustache.context

import com.github.jimkinsey.mustache.context.CanContextualise.Failure
import com.github.jimkinsey.mustache.context.CanContextualiseCaseClass.ConversionFailure
import com.github.jimkinsey.mustache.rendering.Renderer
import Renderer.Context

object CanContextualiseCaseClass {
  case class ConversionFailure(failure: CaseClassConverter.Failure) extends Failure
}

class CanContextualiseCaseClass(converter: CaseClassConverter) extends CanContextualise[Product] {
  def context(obj: Product): Either[Failure, Context] = {
    converter.map(obj).left.map(ConversionFailure.apply)
  }
}
