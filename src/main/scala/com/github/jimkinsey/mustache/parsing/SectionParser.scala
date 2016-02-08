package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.components.{InvertedSection, Template, Section}

import scala.util.matching.Regex

private[mustache] object SectionParser extends ContainerTagParser {
  override lazy val pattern: Regex = """#(.*?)""".r
  override def parsed(name: String, template: Template): Either[Any, Section] = Right(Section(name, template))
}

private[mustache] object InvertedSectionParser extends ContainerTagParser {
  override lazy val pattern: Regex = """\^(.*?)""".r
  override def parsed(name: String, template: Template) = Right(InvertedSection(name, template))
}