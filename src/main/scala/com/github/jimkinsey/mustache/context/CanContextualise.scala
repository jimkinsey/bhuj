package com.github.jimkinsey.mustache.context

import com.github.jimkinsey.mustache.Context

object CanContextualise {
  trait Failure
}

trait CanContextualise[-T] {
  def context(obj: T): Either[CanContextualise.Failure,Context]
}