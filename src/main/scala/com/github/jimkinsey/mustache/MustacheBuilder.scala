package com.github.jimkinsey.mustache

import scala.language.implicitConversions

object MustacheBuilder {
  val mustacheRenderer = MustacheBuilder()

  implicit def mustacheRendererBuilt(builder: MustacheBuilder): Mustache = builder.build
}

case class MustacheBuilder(templatePath: Option[String] = None, cached: Boolean = false) {
  import Caching._

  def withTemplatePath(path: String) = copy(templatePath = Some(path))
  def withCache = copy(cached = true)
  def withoutCache = copy(cached = false)

  lazy val build = {
    val templates: Mustache.Templates = templatePath.map { path =>
      val partialLoader = new FilePartialLoader(path).partial _
      if (cached) partialLoader.withCache else partialLoader
    }.getOrElse(Mustache.emptyTemplates)
    new Mustache(templates)
  }
}
