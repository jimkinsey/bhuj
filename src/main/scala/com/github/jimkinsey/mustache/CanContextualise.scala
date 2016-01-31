package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.Renderer.Context

object CanContextualise {
  trait Failure
  case class ContextualisationFailure(message: String) extends Failure
}

trait CanContextualise[-T] {
  def context(obj: T): Either[CanContextualise.Failure,Context]
}