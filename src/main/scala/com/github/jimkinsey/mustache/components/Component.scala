package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.Context

private[mustache] sealed trait Component {
  def rendered(context: Context): Either[Any, String]
}
private[mustache] trait Value extends Component
private[mustache] trait Container extends Component {
  def template: Template
}

private[mustache] object Template {
  type Result = Either[Any, String]
  val emptyResult: Result = Right("")
}

private[mustache] case class Template(components: Component*) extends Component {
  def append(template: Template) = Template(components ++ template.components :_*)
  def rendered(context: Context) = components.foldLeft(Template.emptyResult) {
    case (Right(acc), component) => component.rendered(context).right.map(acc + _)
    case (failure: Left[Any, String], _) => failure
  }
}
