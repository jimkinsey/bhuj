package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.Mustache.{TemplateNotFound, _}
import com.github.jimkinsey.mustache.context.CanContextualise
import com.github.jimkinsey.mustache.parsing.{InvertedSectionParser, SectionParser, UnescapedVariableParser, VariableParser}

object Mustache {
  trait Failure
  case class TemplateNotFound(name: String) extends Failure

  type Templates = (String => Option[String])
  lazy val emptyTemplates: Templates = Map.empty.get
}

class Mustache(
  templates: Templates = emptyTemplates,
  globalContext: Context = Map.empty) {

  private val templateParser = new TemplateParser(tagParsers = Seq(VariableParser, UnescapedVariableParser, SectionParser, InvertedSectionParser))

  def this(map: Map[String,String]) = {
    this(map.get _)
  }

  def renderTemplate[C](name: String, context: C)(implicit ev: CanContextualise[C]): Either[Any, String] = {
    for {
      template <- templates(name).toRight({TemplateNotFound(name)}).right
      parsed <- templateParser.parse(template).right
      ctx <- ev.context(context).right
      result <- parsed.rendered(ctx).right
    } yield { result }
  }

  def render[C](template: String, context: C)(implicit ev: CanContextualise[C]): Either[Any, String] = {
    for {
      parsed <- templateParser.parse(template).right
      ctx <- ev.context(context).right
      rendered <- parsed.rendered(ctx).right
    } yield { rendered }
  }

  def render(template: String): Either[Any, String] = {
    for {
      parsed <- templateParser.parse(template).right
      rendered <- parsed.rendered(Map.empty).right
    } yield { rendered }
  }

}