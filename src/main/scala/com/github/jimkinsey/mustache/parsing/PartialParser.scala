package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.Mustache.Templates
import com.github.jimkinsey.mustache.components.{Template, Partial}

class PartialParser(
  templates: Templates,
  parsed: (String => Either[Any,Template])) extends ComponentParser[Partial] {

  def parseResult(template: String)(implicit parserConfig: ParserConfig): Either[Any, Option[ParseResult[Partial]]] = {
    Right(for {
      res <- s"\\{\\{>\\s*(.+?)\\s*\\}\\}".r.findPrefixMatchOf(template)
      name = res.group(1)
      remainder = res.after.toString
      partial <- templates(name)
    } yield ParseResult(new Partial(name, parsed(partial).right.get), remainder))
  }
}
