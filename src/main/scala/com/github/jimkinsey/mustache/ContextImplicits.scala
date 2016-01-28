package com.github.jimkinsey.mustache

object ContextImplicits {
  implicit val canContextualiseCaseClass = new CanContextualiseCaseClass
  implicit val canContextualiseMap = new CanContextualiseMap
}
