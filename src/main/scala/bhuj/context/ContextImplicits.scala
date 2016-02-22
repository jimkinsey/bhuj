package bhuj.context

object ContextImplicits {
  implicit val canContextualiseCaseClass = new CanContextualiseCaseClass(new CaseClassConverter)
  implicit val canContextualiseMap = new CanContextualiseMap(new CaseClassConverter)
}
