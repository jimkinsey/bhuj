package bhuj.components

import bhuj.parsing.Delimiters

private[bhuj] case class Variable(name: String) extends Value {
  def formatted(delimiters: Delimiters) = delimiters.tag(name)
}
