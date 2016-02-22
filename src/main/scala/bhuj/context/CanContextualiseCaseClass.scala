package bhuj.context

import bhuj.Context
import bhuj.context.CanContextualise.Failure
import bhuj.context.CanContextualiseCaseClass.ConversionFailure

object CanContextualiseCaseClass {
  case class ConversionFailure(failure: CaseClassConverter.Failure) extends Failure
}

class CanContextualiseCaseClass(converter: CaseClassConverter) extends CanContextualise[Product] {
  def context(obj: Product): Either[Failure, Context] = {
    converter.map(obj).left.map(ConversionFailure.apply)
  }
}
