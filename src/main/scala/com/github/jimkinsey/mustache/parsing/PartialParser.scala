package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.{TemplateParser, Mustache}
import com.github.jimkinsey.mustache.Mustache.TemplateNotFound
import com.github.jimkinsey.mustache.components.Partial

private[mustache] class PartialParser(partials: Mustache.Templates, templateParser: => TemplateParser) extends ValueTagParser {
  override lazy val pattern = """>\s*(.*?)\s*""".r
  override def parsed(name: String) = {
    for {
      unparsed <- partials(name).toRight({TemplateNotFound(name)}).right
      template <- templateParser.parse(unparsed).right
    } yield new Partial(name, template)
  }
}
