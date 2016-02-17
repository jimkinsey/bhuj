package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.Mustache._
import com.github.jimkinsey.mustache.context.{CanContextualise, CanContextualiseMap, CaseClassConverter}
import com.github.jimkinsey.mustache.parsing._
import com.github.jimkinsey.mustache.partials.Caching

object Mustache {
  type Templates = (String => Option[String])
  lazy val emptyTemplates: Templates = Map.empty.get
}

class Mustache(
  templates: Templates = emptyTemplates,
  implicit val globalContext: Context = Map.empty) {

  def this(map: Map[String,String]) = {
    this(map.get _)
  }

  def renderTemplate[C](name: String, context: C)(implicit ev: CanContextualise[C]): Result = {
    for {
      template <- templates(name).toRight({TemplateNotFound(name)}).right
      parsed <- parse(template).right
      ctx <- ev.context(context).left.map(ContextualisationFailure).right
      result <- parsed.rendered(ctx).right
    } yield { result }
  }

  def render[C](template: String, context: C)(implicit ev: CanContextualise[C]): Result = {
    for {
      parsed <- parse(template).right
      ctx <- ev.context(context).left.map(ContextualisationFailure).right
      rendered <- parsed.rendered(ctx).right
    } yield { rendered }
  }

  def render(template: String): Result = {
    for {
      parsed <- parse(template).right
      rendered <- parsed.rendered(Map.empty).right
    } yield { rendered }
  }

  private implicit val canContextualiseMap: CanContextualiseMap = new CanContextualiseMap(new CaseClassConverter)

  private lazy val templateParser: TemplateParser = new TemplateParser(
    TextParser,
    VariableParser,
    TripleDelimitedVariableParser,
    AmpersandPrefixedVariableParser,
    CommentParser,
    SectionParser,
    InvertedSectionParser,
    new PartialParser(this.renderTemplate(_,_)),
    SetDelimitersParser)

  private implicit val parserConfig: ParserConfig = ParserConfig(parse, render[Context], doubleMustaches)

  private lazy val parse = Caching.cached(templateParser.template)

}