package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.Mustache.TemplateNotFound
import com.github.jimkinsey.mustache.rendering.Renderer
import Renderer.Context
import com.github.jimkinsey.mustache.context.CanContextualise
import com.github.jimkinsey.mustache.tags._

import Mustache._

object Mustache {
  trait Failure
  case class TemplateNotFound(name: String) extends Failure

  type Templates = (String => Option[String])
  lazy val emptyTemplates: Templates = Map.empty.get
}

class Mustache(
  templates: Templates = emptyTemplates,
  globalContext: Context = Map.empty) {

  private val renderer = new Renderer(
    tags = Set(
      Variable,
      UnescapedVariable,
      SectionStart,
      InvertedSection,
      Comment,
      new Partial(templates)),
    globalContext = globalContext
  )

  def this(map: Map[String,String]) = {
    this(map.get _)
  }

  def renderTemplate[C](name: String, context: C)(implicit ev: CanContextualise[C]): Either[Any, String] = {
    templates(name)
      .map(Right.apply)
      .getOrElse(Left(TemplateNotFound(name)))
      .right
      .flatMap { template =>
        ev.context(context).right.flatMap(ctx => renderer.render(template, ctx))
      }
  }

  def render[C](template: String, context: C)(implicit ev: CanContextualise[C]): Either[Any, String] = {
    ev.context(context).right.flatMap(ctx => renderer.render(template, ctx))
  }

  def render(template: String): Either[Any, String] = {
    renderer.render(template)
  }

}