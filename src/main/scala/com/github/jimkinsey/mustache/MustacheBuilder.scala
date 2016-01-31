package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.partials.FilePartialLoader
import com.github.jimkinsey.mustache.rendering.Renderer.Context
import com.github.jimkinsey.mustache.partials.Caching._

import scala.language.implicitConversions

object MustacheBuilder {
  val mustacheRenderer = MustacheBuilder()

  implicit def mustacheRendererBuilt(builder: MustacheBuilder): Mustache = builder.build
}

case class MustacheBuilder(
  templatePath: Option[String] = None,
  templates: Option[Map[String,String]] = None,
  cached: Boolean = false,
  globalContext: Context = Map[String,Any]()) {

  def withTemplatePath(path: String) = copy(templatePath = Some(path))
  def withTemplates(templates: (String, String)*) = copy(templates = Some(templates.toMap))
  def withCache = copy(cached = true)
  def withoutCache = copy(cached = false)

  lazy val build = {
    val partials: Mustache.Templates =
      templatePath
        .map(path => new FilePartialLoader(path).partial _)
        .orElse(templates.map(_.get _))
        .map(fn => if (cached) fn.withCache else fn)
        .getOrElse(Mustache.emptyTemplates)
    new Mustache(partials, globalContext)
  }
}
