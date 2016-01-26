package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.Renderer.Context

object Contextualiser {
  trait Failure
  case class ContextualisationFailure(message: String) extends Failure
}

trait Contextualiser[T] {
  def context(obj: T): Either[Contextualiser.Failure,Context]
}