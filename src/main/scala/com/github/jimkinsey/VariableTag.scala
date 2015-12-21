package com.github.jimkinsey

import com.github.jimkinsey.Renderer.Tag

object VariableTag extends Tag {
  val pattern = """^([^\{#].*)$""".r
  def process(name: String, context: Renderer.Context, postTagTemplate: String, render: ((String, Renderer.Context) => Renderer.Result)) =
    Right((context.get(name).map(_.toString).map(escapeHTML).getOrElse("")), postTagTemplate)

  private def escapeHTML(str: String) = str.foldLeft("") { case (acc, char) => acc + escapeCode.getOrElse(char, char) }

  private lazy val escapeCode = Map(
    '<' -> "&lt;",
    '>' -> "&gt;",
    '&' -> "&amp;",
    '\"' -> "&quot;",
    ''' -> "&#39;"
  )
}
