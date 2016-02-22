package bhuj.parsing

import bhuj.components._
import bhuj._

private[bhuj] case class ParseResult[+T <: Component](component: T, remainder: String)

private[bhuj] trait ComponentParser[+T <: Component] {
  def parseResult(template: String)(implicit parserConfig: ParserConfig): Either[Failure, Option[ParseResult[T]]]
}

private[bhuj] case class ParserConfig(
  parsed: (String) => Either[Failure, Template],
  rendered: Render,
  delimiters: Delimiters = doubleMustaches
)

private[bhuj] class TemplateParser(componentParsers: ComponentParser[Component]*) {

  def template(raw: String)(implicit parserConfig: ParserConfig): Either[Failure, Template] = {
    Stream(componentParsers:_*).map(_.parseResult(raw)).collectFirst {
      case Right(Some(ParseResult(directive: ParserDirective, remainder))) =>
        template(remainder)(directive.modified).right.map(tail => Template(parserConfig.delimiters, directive +: tail.components :_*))
      case Right(Some(ParseResult(head, remainder))) =>
        template(remainder).right.map(tail => Template(parserConfig.delimiters, head +: tail.components :_*))
      case Left(fail) =>
        Left(fail)
    }.getOrElse(Right(Template(parserConfig.delimiters)))
  }

}
