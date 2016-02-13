package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.components.Text

private[mustache] object TextParser extends ComponentParser[Text] {
  def parseResult(template: String)(implicit parserConfig: ParserConfig): Either[Any, Option[ParseResult[Text]]] = {
    if (template == "") return Right(None)
    val i = template.indexOf("{{")
    if (i < 0) return Right(Some(ParseResult(Text(template), "")))
    if (i > 0) return Right(Some(ParseResult(Text(template.substring(0, i)), template.substring(i))))
    return Right(None)
  }
}
