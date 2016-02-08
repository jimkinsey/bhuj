package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.components.{Container, Template, Value}

import scala.util.matching.Regex

object TagParser {
  trait Failure
}

sealed trait TagParser {
  def pattern: Regex
}

trait ValueTagParser extends TagParser {
  def parsed(name: String): Either[Any, Value]
}

trait ContainerTagParser extends TagParser {
  def parsed(name: String, template: Template): Either[Any, Container]
}