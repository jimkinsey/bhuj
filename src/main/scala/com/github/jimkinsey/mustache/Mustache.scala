package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.Mustache.{TemplateNotFound, _}
import com.github.jimkinsey.mustache.context.{CaseClassConverter, CanContextualiseMap, CanContextualise}
import com.github.jimkinsey.mustache.parsing._
import com.github.jimkinsey.mustache.partials.Caching

object Mustache {
  trait Failure
  case class TemplateNotFound(name: String) extends Failure

  type Templates = (String => Option[String])
  lazy val emptyTemplates: Templates = Map.empty.get
}

class Mustache(
  templates: Templates = emptyTemplates,
  implicit val globalContext: Context = Map.empty) {

  def this(map: Map[String,String]) = {
    this(map.get _)
  }

  def renderTemplate[C](name: String, context: C)(implicit ev: CanContextualise[C]): Either[Any, String] = {
    for {
      template <- templates(name).toRight({TemplateNotFound(name)}).right
      parsed <- parse(template).right
      ctx <- ev.context(context).right
      result <- parsed.rendered(ctx).right
    } yield { result }
  }

  def render[C](template: String, context: C)(implicit ev: CanContextualise[C]): Either[Any, String] = {
    for {
      parsed <- parse(template).right
      ctx <- ev.context(context).right
      rendered <- parsed.rendered(ctx).right
    } yield { rendered }
  }

  def render(template: String): Either[Any, String] = {
    for {
      parsed <- parse(template).right
      rendered <- parsed.rendered(Map.empty).right
    } yield { rendered }
  }

  private lazy val templateParser: TemplateParser = new TemplateParser(
    TextParser,
    VariableParser,
    UnescapedVariableParser,
    CommentParser,
    SectionParser,
    InvertedSectionParser,
    new PartialParser(this.renderTemplate(_,_)(new CanContextualiseMap(new CaseClassConverter))))

  private lazy val parse = Caching.cached(templateParser.template)

}