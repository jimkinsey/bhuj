package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.Mustache.TemplateNotFound
import com.github.jimkinsey.mustache.Renderer.Failure
import tags._

object Mustache {
  case class TemplateNotFound(name: String) extends Failure
}

class Mustache(templates: Map[String,String] = Map.empty) extends Renderer(tags = Set(
  Variable,
  UnescapedVariable,
  SectionStart,
  InvertedSection,
  Comment,
  new Partial(templates))) {

  def renderTemplate(name: String, context: Map[String, Any] = Map.empty): Either[Failure, String] = {
    templates
      .get(name)
      .map(template => render(template, context))
      .getOrElse(Left(TemplateNotFound(name)))
  }
}