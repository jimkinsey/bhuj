package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.CanContextualise.Failure
import com.github.jimkinsey.mustache.CanContextualiseCaseClass.NotACaseClass
import com.github.jimkinsey.mustache.Renderer.Context

object CanContextualiseCaseClass {
  case class NotACaseClass(obj: Product) extends Failure
}

class CanContextualiseCaseClass extends CanContextualise[Product] {
  def context(obj: Product): Either[Failure, Context] = {
    if (isCaseClass(obj)) {
      val fields = publicFields(obj).map{ f =>
        f.setAccessible(true);
        f.getName -> f.get(obj)
      }
      Right(fields.toMap)
    }
    else {
      Left(NotACaseClass(obj))
    }
  }

  private def publicFields(obj: Product) = {
    obj.getClass.getDeclaredFields.filterNot(_.getName == "$outer")
  }

  private def isCaseClass(obj: Product) = {
    publicFields(obj).length == obj.productArity
  }
}
