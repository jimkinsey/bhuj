package com.github.jimkinsey

import com.github.jimkinsey.Renderer.Tag

object UnescapedVariableTag extends Tag {
  val pattern = """^\{(.+)$""".r
  def process(name: String, context: Renderer.Context, postTagTemplate: String, render: ((String, Renderer.Context) => Renderer.Result)) =
    Right((context.get(name).map(_.toString).getOrElse("")), postTagTemplate)
}
