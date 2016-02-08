package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.components.{Container, Template, Value}

import scala.util.matching.Regex

private[mustache] object TagParser {
  trait Failure
}

private[mustache] sealed trait TagParser {
  def pattern: Regex
}

private[mustache] trait ValueTagParser extends TagParser {
  def parsed(name: String): Either[Any, Value]
}

private[mustache] trait ContainerTagParser extends TagParser {
  def parsed(name: String, template: Template): Either[Any, Container]
}