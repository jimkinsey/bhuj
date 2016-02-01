package com.github.jimkinsey.mustache.rendering

import com.github.jimkinsey.mustache.rendering.Renderer._

import scala.util.matching.Regex
import scala.util.matching.Regex.Match

private[mustache] case class Template(components: Component*)

private[mustache] object Component {
  trait Failure
}

private[mustache] trait Component {
//  def rendered(context: Context): Either[Component.Failure, String]
  def rendered: Either[Component.Failure, String]
}

private[mustache] object Renderer {
  type Result = Either[Failure, String]
  type Context = Map[String, Any]

//  type ContextList = Iterable[Context]

  val emptyResult: Result = Right("")

  trait Failure
//  case class UnrecognisedTag(tag: String) extends Failure

  case class RenderFailure(failure: Component.Failure) extends Failure

  trait Tag {
    def pattern: Regex
    def process(name: String, context: Context, postTagTemplate: String, render: ((String, Context) => Renderer.Result)): Either[Failure, (String, String)]
  }
}

private[mustache] class Renderer {
  def render(template: Template): Result = template
    .components
    .foldLeft(emptyResult) {
      case (Right(rendered), component) =>
        component.rendered
          .right.map(rendered + _)
          .left.map(RenderFailure.apply)
      case (failure: Left[Failure, String], _) =>
        failure
    }
}
//
//private[mustache] class Renderer(tags: Set[Tag], globalContext: Context = Map.empty) {
//
//  def render(template: String, context: Context): Result = {
//    TagPattern.findFirstMatchIn(template).map {
//      case FoundTag((preTag, tagContent, postTag)) =>
//        processTag(tagContent, postTag.getOrElse(""), globalContext ++ context).right.map(preTag + _)
//    }.getOrElse(Right(template))
//  }
//
//  def render(template: String): Result = render(template, Map.empty)
//
//  private lazy val TagPattern = """(?s)(.*?)\{\{(.+?)\}\}([^\}].*){0,1}""".r
//
//  private def processTag(tagContent: String, remainingTemplate: String, context: Context): Result = {
//    object MatchingTag {
//      def unapply(t: Tag): Option[(String, Tag)] = {
//        t.pattern.findFirstMatchIn(tagContent).map(_.group(1) -> t)
//      }
//    }
//
//    tags.collectFirst {
//      case MatchingTag((name, tag: Tag)) =>
//        tag
//          .process(name, context, remainingTemplate, render)
//          .right
//          .flatMap { case (rendered, remaining) => render(remaining, context).right.map(rendered + _) }
//    }.getOrElse(Left(UnrecognisedTag(tagContent)))
//  }
//
//  private object FoundTag {
//    def unapply(m: Match): Option[(String, String, Option[String])] = {
//      Some((m.group(1), m.group(2), Option(m.group(3))))
//    }
//  }
//
//}