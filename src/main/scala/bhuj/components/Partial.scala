package bhuj.components

import bhuj.components.Partial.RenderTemplate
import bhuj.parsing.Delimiters
import bhuj.{Context, Result}

private[bhuj] object Partial {
  type RenderTemplate = (String, Context) => Result
}

private[bhuj] case class Partial(name: String, render: RenderTemplate) extends Value {
  def formatted(delimiters: Delimiters) = delimiters.tag(s"> $name")
}
