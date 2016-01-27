package com.github.jimkinsey.mustache

class CanContextualiseMap extends CanContextualise[Map[String, Any]] {
  def context(map: Map[String, Any]) = Right(map)
}
