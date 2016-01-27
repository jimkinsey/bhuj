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
      Right(map(obj))
    }
    else {
      Left(NotACaseClass(obj))
    }
  }

  private def map(caseClass: Product): Map[String, Any] = {
    val fields = publicFields(caseClass).map{ f =>
      f.setAccessible(true);
      f.getName -> value(f.get(caseClass))
    }
    fields.toMap
  }

  private def value(obj: Any): Any = obj match {
    case product: Product if isCaseClass(product) =>
      map(obj.asInstanceOf[Product])
    case iterable: Iterable[_] =>
      iterable.map(value)
    case _ =>
      obj
  }

  private def publicFields(obj: Product) = {
    obj.getClass.getDeclaredFields.filterNot(_.getName == "$outer")
  }

  private def isCaseClass(obj: Product) = {
    publicFields(obj).length == obj.productArity
  }
}
