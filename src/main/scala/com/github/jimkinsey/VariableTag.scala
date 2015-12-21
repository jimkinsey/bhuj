package com.github.jimkinsey

import com.github.jimkinsey.Renderer.{Result, Context, Tag}

object VariableTag extends Tag {
  val pattern = """^([^\{#].*)$""".r
  def process(name: String, context: Context, postTagTemplate: String, render: ((String, Context) => Result)) =
    Right(context.get(name).map(_.toString).map(escapeHTML).getOrElse(""), postTagTemplate)

  private def escapeHTML(str: String) = str.foldLeft("") { case (acc, char) => acc + escapeCode.getOrElse(char, char) }

  private lazy val escapeCode = Map(
    '<' -> "&lt;",
    '>' -> "&gt;",
    '&' -> "&amp;",
    '\"' -> "&quot;",
    ''' -> "&#39;"
  )
}
