package bhuj.parsing

import bhuj.components.Partial

private[bhuj] class PartialParser(rendered: Partial.RenderTemplate) extends ComponentParser[Partial] {
  def parseResult(template: String)(implicit parserConfig: ParserConfig) = {
    Right(for {
      res <- parserConfig.delimiters.pattern(s">\\s*(.+?)\\s*").r.findPrefixMatchOf(template)
      name = res.group(1)
      remainder = res.after.toString
    } yield ParseResult(new Partial(name, rendered), remainder))
  }
}
