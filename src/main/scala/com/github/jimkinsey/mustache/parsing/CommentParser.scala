package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.components.{Comment, Value}

import scala.util.matching.Regex

object CommentParser extends ValueTagParser {
  override lazy val pattern: Regex = """(?s)\!(.*)""".r
  override def parsed(name: String): Either[Any, Value] = Right(Comment(name))
}
