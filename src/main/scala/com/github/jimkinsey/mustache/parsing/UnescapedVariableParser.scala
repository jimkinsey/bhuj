package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.components.UnescapedVariable

private[mustache] object UnescapedVariableParser extends ValueTagParser {
  override lazy val pattern = """\{([a-zA-Z_][a-zA-Z_0-9]*)\}""".r
  override def parsed(name: String) = Right(UnescapedVariable(name))
}
