package com.github.jimkinsey.mustache.tags

import com.github.jimkinsey.mustache.rendering.Renderer
import Renderer._

object Variable extends Tag {
  val pattern = """^([^\{#>\^!].*)$""".r
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
