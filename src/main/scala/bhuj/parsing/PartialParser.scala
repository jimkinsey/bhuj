package bhuj.parsing

import bhuj.model.Partial

private[bhuj] object PartialParser extends ComponentParser[Partial] {
  def parseResult(template: String)(implicit parserConfig: ParserConfig) = {
    Right(for {
      res <- parserConfig.delimiters.pattern(s">\\s*(.+?)\\s*").r.findPrefixMatchOf(template)
      name = res.group(1)
      remainder = res.after.toString
    } yield ParseResult(Partial(name), remainder))
  }
}
