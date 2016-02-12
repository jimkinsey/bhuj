package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.components._

case class ParseResult[+T <: Component](component: T, remainder: String)

trait ComponentParser[+T <: Component] {
  def parseResult(template: String)(implicit parserConfig: ParserConfig): Either[Any, Option[ParseResult[T]]]
}

case class ParserConfig(parsed: (String) => Either[Any, Template])

class TemplateParser(componentParsers: ComponentParser[Component]*) {
  private implicit lazy val parserConfig: ParserConfig = ParserConfig(this.template _)

  def template(raw: String): Either[Any, Template] = {
    Stream(componentParsers:_*).map(_.parseResult(raw)).collectFirst {
      case Right(Some(ParseResult(head, remainder))) =>
        template(remainder).right.map(tail => Template(head).append(tail))
      case Left(fail) =>
        Left(fail)
    }.getOrElse(Right(Template()))
  }

}
