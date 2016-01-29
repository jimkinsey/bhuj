package com.github.jimkinsey.mustache

class CanContextualiseMap(caseClassConverter: CaseClassConverter) extends CanContextualise[Map[String, Any]] {
  def context(map: Map[String, Any]) = {
    Right(map.mapValues(value))
  }

  private def value(obj: Any): Any = {
    obj match {
      case value: Map[String,Any] @unchecked => context(value).right.get
      case value: Iterable[Product] @unchecked => value.map(caseClassConverter.map).map(_.right.get)
      case value: Product => caseClassConverter.map(value).right.getOrElse(value)
      case value => value
    }
  }
}
