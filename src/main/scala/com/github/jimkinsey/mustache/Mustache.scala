package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.Mustache.{TemplateNotFound, _}
import com.github.jimkinsey.mustache.context.CanContextualise
import com.github.jimkinsey.mustache.parsing.VariableParser
import com.github.jimkinsey.mustache.rendering.Renderer
import com.github.jimkinsey.mustache.rendering.Renderer.Context

object Mustache {
  trait Failure
  case class TemplateNotFound(name: String) extends Failure

  type Templates = (String => Option[String])
  lazy val emptyTemplates: Templates = Map.empty.get
}

class Mustache(
  templates: Templates = emptyTemplates,
  globalContext: Context = Map.empty) {

  private val templateParser = new TemplateParser(tagParsers = Seq(VariableParser))
  private val renderer = new Renderer()

  def this(map: Map[String,String]) = {
    this(map.get _)
  }

  def renderTemplate[C](name: String, context: C)(implicit ev: CanContextualise[C]): Either[Any, String] = {
    for {
      template <- templates(name).toRight({TemplateNotFound(name)}).right
      result <- render(template, context).right
    } yield { result }
  }

  def render[C](template: String, context: C)(implicit ev: CanContextualise[C]): Either[Any, String] = {
    for {
      parsed <- templateParser.parse(template).right
      ctx <- ev.context(context).right
      rendered <- renderer.render(parsed, ctx).right
    } yield { rendered }
  }

  def render(template: String): Either[Any, String] = {
    for {
      parsed <- templateParser.parse(template).right
      rendered <- renderer.render(parsed).right
    } yield { rendered }
  }

}