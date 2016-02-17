package com.github.jimkinsey

import com.github.jimkinsey.mustache.parsing.Delimiters

package object mustache {
  type Context = Map[String, Any]
  type Result = Either[Failure, String]
  type Lambda = (String, NonContextualRender) => Result
  type Render = (String, Context) => Result

  private[mustache] val emptyResult: Result = Right("")

  private[mustache] val doubleMustaches = Delimiters("{{", "}}")

  private type NonContextualRender = (String) => Result
}


