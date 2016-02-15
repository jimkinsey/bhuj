package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.parsing.{Delimiters, ParserConfig}

private[mustache] case class SetDelimiters(delimiters: Delimiters) extends ParserDirective {
  def modified(implicit config: ParserConfig): ParserConfig = config.copy(delimiters = delimiters)
  def formatted = s"{{=${delimiters.start} ${delimiters.end}=}}"
}