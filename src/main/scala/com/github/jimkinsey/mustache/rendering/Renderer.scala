package com.github.jimkinsey.mustache.rendering

import com.github.jimkinsey.mustache.components.Section
import com.github.jimkinsey.mustache.rendering.Renderer._

import scala.util.matching.Regex

private[mustache] case class Template(components: Component*) {
  def append(template: Template) = Template(components ++ template.components :_*)
}

private[mustache] sealed trait Component
private[mustache] trait Value extends Component {
  def rendered(context: Context): Either[Any, String]
}
private[mustache] trait Container extends Component {
  def rendered(context: Context, render: Section.Render): Either[Any, String]
}

private[mustache] object Renderer {
  type Result = Either[Failure, String]
  type Context = Map[String, Any]

  val emptyResult: Result = Right("")

  trait Failure
  case class RenderFailure(failure: Any) extends Failure

  trait Tag {
    def pattern: Regex
    def process(name: String, context: Context, postTagTemplate: String, render: ((String, Context) => Renderer.Result)): Either[Failure, (String, String)]
  }
}

private[mustache] class Renderer {
  def render(template: Template, context: Context = Map.empty): Result = template
    .components
    .foldLeft(emptyResult) {
      case (Right(rendered), component) => (component match {
        case component: Value => component.rendered(context)
        case component: Container => component.rendered(context, render _)
      }).right.map(rendered + _)
        .left.map(RenderFailure.apply)
      case (failure: Left[Failure, String], _) => failure
    }
}