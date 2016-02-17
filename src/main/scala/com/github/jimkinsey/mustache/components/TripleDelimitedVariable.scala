package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.Context
import com.github.jimkinsey.mustache.parsing.Delimiters

private[mustache] trait UnescapedVariable extends Value {
  def name: String
  final def rendered(context: Context)(implicit global: Context) = {
    Right((global ++ context).get(name).map(_.toString).getOrElse(""))
  }
}

private[mustache] case class TripleDelimitedVariable(name: String) extends UnescapedVariable {
  def formatted(delimiters: Delimiters) = delimiters.tag(s"{$name}")
}

private[mustache] case class AmpersandPrefixedVariable(name: String) extends UnescapedVariable {
  def formatted(delimiters: Delimiters) = delimiters.tag(s"&$name")
}
