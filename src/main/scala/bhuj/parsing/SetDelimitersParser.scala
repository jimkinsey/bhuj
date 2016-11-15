package bhuj.parsing

import bhuj.{ParseTemplateFailure, Failure, InvalidDelimiters}
import bhuj.model.SetDelimiters

object SetDelimitersParser extends ComponentParser[SetDelimiters] {
  def parseResult(template: String)(implicit parserConfig: ParserConfig) = {
    parserConfig.delimiters.pattern(s"""=(.+?) (.+?)=""").r.findFirstMatchIn(template).fold(emptyResult) {
      case m if valid(m.group(1), m.group(2))=>
        Right(Some(ParseResult(SetDelimiters(Delimiters(m.group(1), m.group(2))), m.after.toString)))
      case m  =>
        Left(InvalidDelimiters(m.group(1), m.group(2)))
    }
  }

  private val emptyResult: Either[ParseTemplateFailure, Option[ParseResult[SetDelimiters]]] = Right(None)

  private def valid(start: String, end: String) = {
    start.matches(validDelimiter) && end.matches(validDelimiter)
  }

  private val validDelimiter = "[^\\s=]+"
}
