package com.github.jimkinsey.mustache.tags

import com.github.jimkinsey.mustache.Renderer.{Failure, Result, Context, Tag}
import com.github.jimkinsey.mustache.tags.Partial.PartialNotFound

object Partial {
  case class PartialNotFound(name: String) extends Failure
}

class Partial(partials: Map[String,String] = Map.empty) extends Tag {
  val pattern = """^>\s*([^\s]+)\s*$""".r

  def process(name: String, context: Context, postTagTemplate: String, render: (String, Context) => Result): Either[Failure, (String, String)] =
    partials
      .get(name)
      .map(template => render(template, context).right.map(_ -> postTagTemplate))
      .getOrElse(Left(PartialNotFound(name)))
}
