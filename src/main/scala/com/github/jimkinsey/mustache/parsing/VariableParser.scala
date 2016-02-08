package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.components.Variable

private[mustache] object VariableParser extends ValueTagParser {
  override lazy val pattern = """([a-zA-Z_][a-zA-Z_0-9]*)""".r
  override def parsed(name: String) = Right(Variable(name))
}


