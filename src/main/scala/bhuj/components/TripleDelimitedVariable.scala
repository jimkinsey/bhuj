package bhuj.components

import bhuj.parsing.Delimiters

private[bhuj] trait UnescapedVariable extends Value {
  def name: String
}

private[bhuj] case class TripleDelimitedVariable(name: String) extends UnescapedVariable {
  def formatted(delimiters: Delimiters) = delimiters.tag(s"{$name}")
}

private[bhuj] case class AmpersandPrefixedVariable(name: String) extends UnescapedVariable {
  def formatted(delimiters: Delimiters) = delimiters.tag(s"&$name")
}
