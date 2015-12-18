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

  private def processTag(tag: String, remainingTemplate: String, context: Context): Result = tag match {
    case Variable(name) =>
      render(remainingTemplate, context).right.map(context.get(name).map(_.toString).map(escapeHTML).getOrElse("") + _)
    case UnescapedVariable(name) =>
      render(remainingTemplate, context).right.map(context.get(name).map(_.toString).getOrElse("") + _)
    case SectionStart(name) =>
      ("""(?s)(.*?)\{\{/""" + Pattern.quote(name) + """\}\}(.*)""").r.findFirstMatchIn(remainingTemplate).map(m => (m.group(1), m.group(2))).map { case (sectionTemplate, postSectionTemplate) =>
        render(postSectionTemplate, context).right.flatMap{ (remaining) => {
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
          }.getOrElse(Right("")).right.map(_ + remaining)
        }
      }
    }.getOrElse(Left(UnclosedSection(name)))
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