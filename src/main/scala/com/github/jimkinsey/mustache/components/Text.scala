package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.rendering.Value
import com.github.jimkinsey.mustache.rendering.Renderer.Context

case class Text(content: String) extends Value {
  def rendered(context: Context) = Right(content)
}
