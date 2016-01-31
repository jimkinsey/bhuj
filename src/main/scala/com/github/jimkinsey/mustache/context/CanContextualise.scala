package com.github.jimkinsey.mustache.context

import com.github.jimkinsey.mustache.rendering.Renderer
import Renderer.Context

object CanContextualise {
  trait Failure
  case class ContextualisationFailure(message: String) extends Failure
}

trait CanContextualise[-T] {
  def context(obj: T): Either[CanContextualise.Failure,Context]
}