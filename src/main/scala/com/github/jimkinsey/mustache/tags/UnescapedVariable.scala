package com.github.jimkinsey.mustache.tags

import com.github.jimkinsey.mustache.Renderer._

object UnescapedVariable extends Tag {
  val pattern = """^\{(.+)$""".r
  def process(name: String, context: Context, postTagTemplate: String, render: ((String, Context) => Result)) =
    Right(context.get(name).map(_.toString).getOrElse(""), postTagTemplate)
}
