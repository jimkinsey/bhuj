package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.Context
import com.github.jimkinsey.mustache.parsing.Delimiters

private[mustache] case class Text(content: String) extends Value {
  def rendered(context: Context)(implicit global: Context) = Right(content)
  def formatted(delimiters: Delimiters) = content
}
