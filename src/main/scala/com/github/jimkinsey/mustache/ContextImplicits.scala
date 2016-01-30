package com.github.jimkinsey.mustache

object ContextImplicits {
  implicit val canContextualiseCaseClass = new CanContextualiseCaseClass(new CaseClassConverter)
  implicit val canContextualiseMap = new CanContextualiseMap(new CaseClassConverter)
}
