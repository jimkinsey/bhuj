package com.github.jimkinsey.mustache.tags

import com.github.jimkinsey.mustache.rendering.Renderer
import Renderer._

private[mustache] object Comment extends Tag {
  val pattern = """(?s)!(.+)""".r
  def process(name: String, context: Context, postTagTemplate: String, render: (String, Context) => Result): Either[Failure, (String, String)] =
    Right("", postTagTemplate)
}
