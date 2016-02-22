package bhuj.components

import bhuj.{Result, Failure, Context}
import bhuj.components.Partial.RenderTemplate
import bhuj.parsing.Delimiters

private[bhuj] object Partial {
  type RenderTemplate = (String, Context) => Result
}

private[bhuj] class Partial(val name: String, val render: RenderTemplate) extends Value {
  def rendered(context: Context)(implicit global: Context) = render(name, context)
  def formatted(delimiters: Delimiters) = delimiters.tag(s"> $name")
}
