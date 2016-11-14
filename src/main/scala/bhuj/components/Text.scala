package bhuj.components

import bhuj.parsing.Delimiters

private[bhuj] case class Text(content: String) extends Value {
  def formatted(delimiters: Delimiters) = content
}
