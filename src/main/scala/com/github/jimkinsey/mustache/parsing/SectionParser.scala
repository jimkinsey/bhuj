package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.components.Section
import com.github.jimkinsey.mustache.rendering.Template

import scala.util.matching.Regex

private[mustache] object SectionParser extends ContainerTagParser {
  override lazy val pattern: Regex = """#(.*?)""".r
  override def parsed(name: String, template: Template): Either[Any, Section] = Right(Section(name, template))
}
