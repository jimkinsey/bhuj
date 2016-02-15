package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.Context
import com.github.jimkinsey.mustache.components.Partial.Render

private[mustache] object Partial {
  type Render = (String, Context) => Either[Any, String]
}

private[mustache] class Partial(val name: String, val render: Render) extends Value {
  def rendered(context: Context)(implicit global: Context) = render(name, context)
  lazy val formatted = s"{{> $name}}"
}
