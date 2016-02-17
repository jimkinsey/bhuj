package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache
import com.github.jimkinsey.mustache.{doubleMustaches, Context}
import com.github.jimkinsey.mustache.components._

private[mustache] case class ParseResult[+T <: Component](component: T, remainder: String)

private[mustache] trait ComponentParser[+T <: Component] {
  def parseResult(template: String)(implicit parserConfig: ParserConfig): Either[Any, Option[ParseResult[T]]]
}

private[mustache] case class ParserConfig(
  parsed: (String) => Either[Any, Template],
  rendered: (String, Context) => Either[Any, String],
  delimiters: Delimiters = doubleMustaches
)

private[mustache] class TemplateParser(componentParsers: ComponentParser[Component]*) {

  def template(raw: String)(implicit parserConfig: ParserConfig): Either[Any, Template] = {
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
