package bhuj.components

import bhuj.Context
import bhuj.parsing.Delimiters

private[bhuj] case class Text(content: String) extends Value {
  def rendered(context: Context)(implicit global: Context) = Right(content)
  def formatted(delimiters: Delimiters) = content
}
