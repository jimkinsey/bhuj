package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.TagParser
import com.github.jimkinsey.mustache.components.{UnescapedVariable, Variable}

private[mustache] object UnescapedVariableParser extends TagParser {
  override lazy val pattern = """^\{([a-zA-Z_][a-zA-Z_0-9]*)\}$""".r
  override def parsed(name: String) = Right(UnescapedVariable(name))
}
