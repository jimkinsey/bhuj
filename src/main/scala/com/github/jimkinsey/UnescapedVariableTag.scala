package com.github.jimkinsey

import com.github.jimkinsey.Renderer.{Result, Context, Tag}

object UnescapedVariableTag extends Tag {
  val pattern = """^\{(.+)$""".r
  def process(name: String, context: Context, postTagTemplate: String, render: ((String, Context) => Result)) =
    Right(context.get(name).map(_.toString).getOrElse(""), postTagTemplate)
}
