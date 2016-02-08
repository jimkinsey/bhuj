package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.Context

case class Text(content: String) extends Value {
  def rendered(context: Context)(implicit global: Context) = Right(content)
}
