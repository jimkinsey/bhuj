package bhuj.context

class CanContextualiseMap(caseClassConverter: CaseClassConverter) extends CanContextualise[Map[String, Any]] {
  def context(map: Map[String, Any]) = {
    Right(map.mapValues(value))
  }

  private def value(obj: Any): Any = {
    obj match {
      case map: Map[String,Any] @unchecked => map.mapValues(value)
      case iterable: Iterable[Product] @unchecked => iterable.map(value)
      case option: Option[Any] @unchecked => option.map(value)
      case product: Product => caseClassConverter.map(product).right.getOrElse(product)
      case value => value
    }
  }
}
