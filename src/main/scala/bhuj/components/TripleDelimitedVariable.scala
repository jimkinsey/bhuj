package bhuj.components

import bhuj.Context
import bhuj.parsing.Delimiters

private[bhuj] trait UnescapedVariable extends Value {
  def name: String
  final def rendered(context: Context)(implicit global: Context) = {
    Right((global ++ context).get(name).map(_.toString).getOrElse(""))
  }
}

private[bhuj] case class TripleDelimitedVariable(name: String) extends UnescapedVariable {
  def formatted(delimiters: Delimiters) = delimiters.tag(s"{$name}")
}

private[bhuj] case class AmpersandPrefixedVariable(name: String) extends UnescapedVariable {
  def formatted(delimiters: Delimiters) = delimiters.tag(s"&$name")
}
