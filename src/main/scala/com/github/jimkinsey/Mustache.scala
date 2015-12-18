package com.github.jimkinsey

import java.util.regex.Pattern

import scala.util.matching.Regex

class Mustache {
  def render(template: String, context: Context = Map.empty): String = {
    TagPattern.findFirstMatchIn(template).map {
      m => m.group(1) + processTag(m.group(2), Option(m.group(3)).getOrElse(""), context)
    }.getOrElse(template)
  }

  private lazy val TagPattern = """(?s)(.*?)\{\{(.+?)\}\}([^\}].*){0,1}""".r

  private type Context = Map[String, Any]
  private type ContextList = Iterable[Context]
  private type Lambda = (String, (String => String)) => String

  private def processTag(tag: String, remainingTemplate: String, context: Context): String = tag match {
    case Variable(name) =>
      context.get(name).map(_.toString).map(escapeHTML).getOrElse("") + render(remainingTemplate, context)
    case UnescapedVariable(name) =>
      context.get(name).map(_.toString).getOrElse("") + render(remainingTemplate, context)
    case SectionStart(name) =>
      val (sectionTemplate, postSectionTemplate) = ("""(?s)(.*?)\{\{/""" + Pattern.quote(name) + """\}\}(.*)""").r.findFirstMatchIn(remainingTemplate).map(m => (m.group(1), m.group(2))).get
      context.get(name).collect {
        case nonFalseValue: Context => render(sectionTemplate, nonFalseValue)
        case iterable: ContextList if iterable.nonEmpty => iterable.map(item => render(sectionTemplate, item)).mkString
        case lambda: Lambda => lambda(sectionTemplate, render(_, context))
      }.getOrElse("") + render(postSectionTemplate, context)
  }
  
  private object Variable extends TagNameMatcher("""^([^\{#].*)$""".r)
  private object UnescapedVariable extends TagNameMatcher("""^\{(.+)$""".r)
  private object SectionStart extends TagNameMatcher("""^#(.+)$""".r)

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