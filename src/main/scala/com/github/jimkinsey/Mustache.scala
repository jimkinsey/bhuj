package com.github.jimkinsey

import scala.util.matching.Regex.Match

class Mustache {
  def render(template: String, context: Map[String, Any] = Map.empty): String = {
    """\{\{(.+?)\}\}""".r.findFirstMatchIn(template).map {
      case m: Match => template.substring(0, m.start) + context.get(m.group(1)).map(replacement => escapeHTML(replacement.toString)).getOrElse("") + render(template.substring(m.end), context)
    }.getOrElse(template)
  }

  private def escapeHTML(str: String) = str.foldLeft("") { case (acc, char) => acc + escapeCode.getOrElse(char, char) }

  private lazy val escapeCode = Map(
    '<' -> "&lt;",
    '>' -> "&gt;",
    '&' -> "&amp;",
    '\"' -> "&quot;",
    ''' -> "&#39;"
  )

}