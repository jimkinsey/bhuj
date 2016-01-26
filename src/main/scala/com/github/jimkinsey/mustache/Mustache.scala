package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.Mustache.TemplateNotFound
import com.github.jimkinsey.mustache.tags._

object Mustache {
  trait Failure
  case class TemplateNotFound(name: String) extends Failure
}

class Mustache(templates: (String => Option[String]) = Map.empty.get) {
  private val renderer = new Renderer(tags = Set(
    Variable,
    UnescapedVariable,
    SectionStart,
    InvertedSection,
    Comment,
    new Partial(templates))
  )

  def this(map: Map[String,String]) = {
    this(map.get _)
  }

  def renderTemplate[C](name: String, context: C)(implicit ev: Contextualiser[C]): Either[Any, String] = {
    templates(name)
      .map(Right.apply)
      .getOrElse(Left(TemplateNotFound(name)))
      .right
      .flatMap { template =>
        ev.context(context).right.flatMap(ctx => renderer.render(template, ctx))
      }
  }

  def render[C](template: String, context: C)(implicit ev: Contextualiser[C]): Either[Any, String] = {
    ev.context(context).right.flatMap(ctx => renderer.render(template, ctx))
  }

  def render(template: String): Either[Any, String] = {
    renderer.render(template)
  }

}