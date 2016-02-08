package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.Context

case class Variable(name: String) extends Value {
  def rendered(context: Context) = Right(context.get(name).map(_.toString).map(escapeHTML).getOrElse(""))

  private def escapeHTML(str: String) = str.foldLeft("") { case (acc, char) => acc + escapeCode.getOrElse(char, char) }

  private lazy val escapeCode = Map(
    '<' -> "&lt;",
    '>' -> "&gt;",
    '&' -> "&amp;",
    '\"' -> "&quot;",
    ''' -> "&#39;"
  )
}
