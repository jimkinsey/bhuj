package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.Context

sealed trait Component {
  def rendered(context: Context)(implicit global: Context): Either[Any, String]
}
trait Value extends Component
trait Container extends Component {
  def template: Template
}

object Template {
  type Result = Either[Any, String]
  val emptyResult: Result = Right("")
}

case class Template(components: Component*) extends Component {
  def append(template: Template) = Template(components ++ template.components :_*)
  def rendered(context: Context)(implicit global: Context) = components.foldLeft(Template.emptyResult) {
    case (Right(acc), component) => component.rendered(global ++ context).right.map(acc + _)
    case (failure: Left[Any, String], _) => failure
  }
}
