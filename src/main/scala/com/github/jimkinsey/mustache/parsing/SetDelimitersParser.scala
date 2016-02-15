package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.components.SetDelimiters

import scala.util.matching.Regex.quote

object SetDelimitersParser extends ComponentParser[SetDelimiters] {
  def parseResult(template: String)(implicit parserConfig: ParserConfig) = {
    val s = quote(parserConfig.delimiters.start)
    val e = quote(parserConfig.delimiters.end)
    val res = s"""$s=(.+?) (.+?)=$e""".r.findFirstMatchIn(template).map { m =>
      ParseResult(SetDelimiters(Delimiters(m.group(1), m.group(2))), m.after.toString)
    }
    Right(res)
  }
}
