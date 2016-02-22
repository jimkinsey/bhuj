package bhuj.context

import bhuj.Context

object CanContextualise {
  trait Failure
}

trait CanContextualise[-T] {
  def context(obj: T): Either[CanContextualise.Failure,Context]
}