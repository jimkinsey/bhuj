package com.github.jimkinsey.mustache.tags

import com.github.jimkinsey.mustache.rendering.Renderer
import Renderer.{Failure, Result, Context, Tag}
import com.github.jimkinsey.mustache.tags.Partial.PartialNotFound

object Partial {
  case class PartialNotFound(name: String) extends Failure
}

class Partial(partials: (String => Option[String]) = _ => None) extends Tag {
  val pattern = """^>\s*([^\s]+)\s*$""".r

  def this(map: Map[String,String]) = {
    this(map.get _)
  }

  def process(name: String, context: Context, postTagTemplate: String, render: (String, Context) => Result): Either[Failure, (String, String)] =
    partials(name)
      .map(template => render(template, context).right.map(_ -> postTagTemplate))
      .getOrElse(Left(PartialNotFound(name)))
}
