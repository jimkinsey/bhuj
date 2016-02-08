package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.rendering.Renderer._
import com.github.jimkinsey.mustache.rendering.Value

case class UnescapedVariable(name: String) extends Value {
  def rendered(context: Context) = Right(context.get(name).map(_.toString).getOrElse(""))
}
