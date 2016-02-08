package com.github.jimkinsey.mustache.rendering

import com.github.jimkinsey.mustache.components.Section
import com.github.jimkinsey.mustache.rendering.Renderer._

private[mustache] object Template {
  type Result = Either[Any, String]
  val emptyResult: Result = Right("")
}

private[mustache] case class Template(components: Component*) extends Component {
  def append(template: Template) = Template(components ++ template.components :_*)
  def rendered(context: Context): Either[Any, String] = components.foldLeft(Template.emptyResult) {
    case (Right(acc), component) => component match {
      case component: Container => component.rendered(context, rendered).right.map(acc + _)
      case _ => component.rendered(context).right.map(acc + _)
    }
    case (failure: Left[Any, String], _) => failure
  }

  private val rendered: Section.Render = (template, context) => template.rendered(context)
}

private[mustache] sealed trait Component {
  def rendered(context: Context): Either[Any, String]
}
private[mustache] trait Value extends Component
private[mustache] trait Container extends Component {
  def rendered(context: Context): Either[Any, String] = Left("We don't serve your kind here")
  def rendered(context: Context, render: Section.Render): Either[Any, String]
}

private[mustache] object Renderer {
  type Result = Template.Result
  type Context = Map[String, Any]

  val emptyResult: Template.Result = Right("")
}
