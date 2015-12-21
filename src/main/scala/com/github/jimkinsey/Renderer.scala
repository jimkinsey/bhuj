package com.github.jimkinsey

import com.github.jimkinsey.Renderer._

import scala.util.matching.Regex
import scala.util.matching.Regex.Match

object Renderer {
  type Result = Either[Failure, String]
  type Context = Map[String, Any]
  type ContextList = Iterable[Context]

  trait Failure
  case class UnrecognisedTag(tag: String) extends Failure

  trait Tag {
    def pattern: Regex
    def process(name: String, context: Renderer.Context, postTagTemplate: String, render: ((String, Renderer.Context) => Renderer.Result)): Either[Failure, (String, String)]
  }
}

class Renderer(tags: Set[Tag]) {

  def render(template: String, context: Context = Map.empty): Result = {
    TagPattern.findFirstMatchIn(template).map {
      case FoundTag((preTag, tagContent, postTag)) =>
        processTag(tagContent, postTag.getOrElse(""), context).right.map(preTag + _)
    }.getOrElse(Right(template))
  }

  private lazy val TagPattern = """(?s)(.*?)\{\{(.+?)\}\}([^\}].*){0,1}""".r

  private def processTag(tagContent: String, remainingTemplate: String, context: Context): Result = {
    object MatchingTag {
      def unapply(t: Tag): Option[(String, Tag)] = {
        t.pattern.findFirstMatchIn(tagContent).map(_.group(1) -> t)
      }
    }

    tags.collectFirst {
      case MatchingTag((name, tag: Tag)) =>
        tag
          .process(name, context, remainingTemplate, render)
          .right
          .flatMap { case (rendered, remaining) => render(remaining, context).right.map(rendered + _) }
    }.getOrElse(Left(UnrecognisedTag(tagContent)))
  }

  private object FoundTag {
    def unapply(m: Match): Option[(String, String, Option[String])] = {
      Some((m.group(1), m.group(2), Option(m.group(3))))
    }
  }

}