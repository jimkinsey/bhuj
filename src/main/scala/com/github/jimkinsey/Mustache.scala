package com.github.jimkinsey

import scala.util.matching.Regex.Match

class Mustache {
  def render(template: String, context: Map[String, Any] = Map.empty): String = {
    """\{\{(.+?)\}\}""".r.findFirstMatchIn(template).map {
      case m: Match => template.substring(0, m.start) + context.getOrElse(m.group(1), "") + render(template.substring(m.end), context)
    }.getOrElse(template)
  }
}