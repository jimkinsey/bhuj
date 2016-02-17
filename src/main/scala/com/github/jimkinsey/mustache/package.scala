package com.github.jimkinsey

import com.github.jimkinsey.mustache.parsing.Delimiters

package object mustache {
  type Context = Map[String, Any]
  type Result = Either[Any, String]
  type Lambda = (String, NonContextualRender) => Result

  val doubleMustaches = Delimiters("{{", "}}")

  private type NonContextualRender = (String) => Result
}
