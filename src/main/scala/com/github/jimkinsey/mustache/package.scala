package com.github.jimkinsey

package object mustache {
  type Context = Map[String, Any]
  type Result = Either[Any, String]
  type Lambda = (String, NonContextualRender) => Result
  private type NonContextualRender = (String) => Result
}
