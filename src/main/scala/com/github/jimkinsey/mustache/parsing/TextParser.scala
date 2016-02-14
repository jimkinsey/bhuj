package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.components.Text

private[mustache] object TextParser extends ComponentParser[Text] {
  def parseResult(template: String)(implicit parserConfig: ParserConfig) = {
    template.indexOf("{{") match {
      case _ if template == "" => Right(None)
      case i if i < 0 => Right(Some(ParseResult(Text(template), "")))
      case i if i > 0 => Right(Some(ParseResult(Text(template.substring(0, i)), template.substring(i))))
      case _ => Right(None)
    }
  }
}
