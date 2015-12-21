package com.github.jimkinsey

import java.util.regex.Pattern

import com.github.jimkinsey.Mustache.{Failure, UnclosedSection}

import scala.util.matching.Regex

object Mustache {
  sealed trait Failure
  case class UnclosedSection(name: String) extends Failure
}

class Mustache {
  def render(template: String, context: Context = Map.empty): Result = {
    TagPattern.findFirstMatchIn(template).map {
      m => processTag(m.group(2), Option(m.group(3)).getOrElse(""), context).right.map(m.group(1) + _)
    }.getOrElse(Right(template))
  }

  private lazy val TagPattern = """(?s)(.*?)\{\{(.+?)\}\}([^\}].*){0,1}""".r

  private type Result = Either[Failure, String]
  private type Context = Map[String, Any]
  private type ContextList = Iterable[Context]
  private type Lambda = (String, (String => Result)) => Result

  private def processTag(tag: String, remainingTemplate: String, context: Context): Result =
  {
    val processedTag = tag match {
      case Variable(name) => processVariable(remainingTemplate, name, context)
      case UnescapedVariable(name) => processUnescapedVariable(remainingTemplate, name, context)
      case SectionStart(name) => processSection(remainingTemplate, name, context)
    }
    processedTag._1.right.flatMap(rendered => render(processedTag._2, context).right.map(rendered + _))
  }

  private def processVariable(remainingTemplate: String, name: String, context: Context): (Result, String) = {
    (Right(context.get(name).map(_.toString).map(escapeHTML).getOrElse("")), remainingTemplate)
  }

  private def processUnescapedVariable(remainingTemplate: String, name: String, context: Context): (Result, String) = {
    (Right(context.get(name).map(_.toString).getOrElse("")), remainingTemplate)
  }

  private def processSection(remainingTemplate: String, name: String, context: Context): (Result, String) = {
    ("""(?s)(.*?)\{\{/""" + Pattern.quote(name) + """\}\}(.*)""").r.findFirstMatchIn(remainingTemplate).map(m => (m.group(1), m.group(2))).flatMap {
      case (sectionTemplate, postSectionTemplate) =>
        context.get(name).collect {
          case nonFalseValue: Context =>
            render(sectionTemplate, nonFalseValue)
          case iterable: ContextList if iterable.nonEmpty =>
            iterable.foldLeft[Result](Right("")) {
              case (Right(acc), item) => render(sectionTemplate, item).right.map(acc + _)
              case (fail, _) => fail
            }
          case lambda: Lambda =>
            lambda(sectionTemplate, render(_, context))
        }.orElse(Some(Right(""))).map(_ -> postSectionTemplate)
    }.getOrElse((Left(UnclosedSection(name)), ""))
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