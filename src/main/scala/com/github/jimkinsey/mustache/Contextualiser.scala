package com.github.jimkinsey.mustache

object Contextualiser {
  trait Failure
  case class ContextualisationFailure(message: String) extends Failure
}

trait Contextualiser[T] {
  def context(obj: T): Either[Contextualiser.Failure,Map[String, Any]]
}