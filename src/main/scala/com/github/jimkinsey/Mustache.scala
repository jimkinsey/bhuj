package com.github.jimkinsey

import scala.util.matching.Regex

class Mustache {
  def render(template: String, context: Map[String, Any] = Map.empty): String = {
    """(?s)(.*?)\{\{(.+?)\}\}([^\}].*){0,1}""".r.findFirstMatchIn(template).map {
      m => m.group(1) + processTag(m.group(2), Option(m.group(3)).getOrElse(""), context)
    }.getOrElse(template)
  }

  private def processTag(tag: String, remainingTemplate: String, context: Map[String, Any]): String = tag match {
    case Variable(name) =>
      context.get(name).map(_.toString).map(escapeHTML).getOrElse("") + render(remainingTemplate, context)
    case UnescapedVariable(name) =>
      context.get(name).map(_.toString).getOrElse("") + render(remainingTemplate, context)
  }
  
  private object Variable extends TagNameMatcher("""^([^\{].+)$""".r)
  private object UnescapedVariable extends TagNameMatcher("""^\{(.+)$""".r)

  private class TagNameMatcher(pattern: Regex) {
    def unapply(tag: String): Option[String] = pattern.findFirstMatchIn(tag).map(_.group(1))
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