package com.github.jimkinsey

import java.util.regex.Pattern

import com.github.jimkinsey.Mustache.{Failure, UnclosedSection}

import scala.util.matching.Regex

object Mustache {
  type Result = Either[Failure, String]
  type Context = Map[String, Any]
  type ContextList = Iterable[Context]
  type Lambda = (String, (String => Result)) => Result

  sealed trait Failure
  case class UnclosedSection(name: String) extends Failure

  def escapeHTML(str: String) = str.foldLeft("") { case (acc, char) => acc + escapeCode.getOrElse(char, char) }

  private lazy val escapeCode = Map(
    '<' -> "&lt;",
    '>' -> "&gt;",
    '&' -> "&amp;",
    '\"' -> "&quot;",
    ''' -> "&#39;"
  )
}

trait Tag {
  def pattern: Regex
  def process(name: String, context: Mustache.Context, postTagTemplate: String, render: ((String, Mustache.Context) => Mustache.Result)): (Mustache.Result, String)
}

object VariableTag extends Tag {
  val pattern = """^([^\{#].*)$""".r
  def process(name: String, context: Mustache.Context, postTagTemplate: String, render: ((String, Mustache.Context) => Mustache.Result)) =
    (Right(context.get(name).map(_.toString).map(Mustache.escapeHTML).getOrElse("")), postTagTemplate)
}

object UnescapedVariableTag extends Tag {
  val pattern = """^\{(.+)$""".r
  def process(name: String, context: Mustache.Context, postTagTemplate: String, render: ((String, Mustache.Context) => Mustache.Result)) =
    (Right(context.get(name).map(_.toString).getOrElse("")), postTagTemplate)
}

object SectionStartTag extends Tag {
  val pattern = """^#(.+)$""".r
  def process(name: String, context: Mustache.Context, postTagTemplate: String, render: ((String, Mustache.Context) => Mustache.Result)) = {
    ("""(?s)(.*?)\{\{/""" + Pattern.quote(name) + """\}\}(.*)""").r.findFirstMatchIn(postTagTemplate).map(m => (m.group(1), m.group(2))).flatMap {
      case (sectionTemplate, postSectionTemplate) =>
        context.get(name).collect {
          case nonFalseValue: Mustache.Context =>
            render(sectionTemplate, nonFalseValue)
          case iterable: Mustache.ContextList if iterable.nonEmpty =>
            iterable.foldLeft[Mustache.Result](Right("")) {
              case (Right(acc), item) => render(sectionTemplate, item).right.map(acc + _)
              case (fail, _) => fail
            }
          case lambda: Mustache.Lambda =>
            lambda(sectionTemplate, render(_, context))
        }.orElse(Some(Right(""))).map(_ -> postSectionTemplate)
    }.getOrElse((Left(UnclosedSection(name)), ""))
  }
}

class Mustache {
  import Mustache._

  def render(template: String, context: Context = Map.empty): Result = {
    TagPattern.findFirstMatchIn(template).map {
      m => processTag(m.group(2), Option(m.group(3)).getOrElse(""), context).right.map(m.group(1) + _)
    }.getOrElse(Right(template))
  }

  private lazy val TagPattern = """(?s)(.*?)\{\{(.+?)\}\}([^\}].*){0,1}""".r

  private def processTag(tag: String, remainingTemplate: String, context: Context): Result = {
    val processedTag = tag match {
      case Variable(name) => processVariable(name, context, remainingTemplate, render)
      case UnescapedVariable(name) => processUnescapedVariable(name, context, remainingTemplate, render)
      case SectionStart(name) => processSection(name, context, remainingTemplate, render)
    }
    processedTag._1.right.flatMap(rendered => render(processedTag._2, context).right.map(rendered + _))
  }

  private def processVariable = VariableTag.process _
  private def processUnescapedVariable = UnescapedVariableTag.process _
  private def processSection = SectionStartTag.process _

  private object Variable extends TagNameMatcher(VariableTag.pattern)
  private object UnescapedVariable extends TagNameMatcher(UnescapedVariableTag.pattern)
  private object SectionStart extends TagNameMatcher(SectionStartTag.pattern)

  private class TagNameMatcher(pattern: Regex) {
    def unapply(tag: String): Option[String] = pattern.findFirstMatchIn(tag).map(_.group(1))
  }

}