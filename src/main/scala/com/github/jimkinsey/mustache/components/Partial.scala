package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.{Result, Failure, Context}
import com.github.jimkinsey.mustache.components.Partial.RenderTemplate
import com.github.jimkinsey.mustache.parsing.Delimiters

private[mustache] object Partial {
  type RenderTemplate = (String, Context) => Result
}

private[mustache] class Partial(val name: String, val render: RenderTemplate) extends Value {
  def rendered(context: Context)(implicit global: Context) = render(name, context)
  def formatted(delimiters: Delimiters) = delimiters.tag(s"> $name")
}
