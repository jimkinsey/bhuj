package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.Context

case class UnescapedVariable(name: String) extends Value {
  def rendered(context: Context) = Right(context.get(name).map(_.toString).getOrElse(""))
}
