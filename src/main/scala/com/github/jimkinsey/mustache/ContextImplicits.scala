package com.github.jimkinsey.mustache

object ContextImplicits {
  implicit val canContextualiseCaseClass = new CanContextualiseCaseClass(null)
  implicit val canContextualiseMap = new CanContextualiseMap(null) // FIXME
}
