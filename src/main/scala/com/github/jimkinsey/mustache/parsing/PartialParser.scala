package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.components.Partial

private[mustache] class PartialParser(rendered: Partial.RenderTemplate) extends ComponentParser[Partial] {
  def parseResult(template: String)(implicit parserConfig: ParserConfig) = {
    Right(for {
      res <- parserConfig.delimiters.pattern(s">\\s*(.+?)\\s*").r.findPrefixMatchOf(template)
      name = res.group(1)
      remainder = res.after.toString
    } yield ParseResult(new Partial(name, rendered), remainder))
  }
}
