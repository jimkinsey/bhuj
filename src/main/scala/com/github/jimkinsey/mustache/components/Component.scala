package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.Context
import com.github.jimkinsey.mustache.parsing.ParserConfig

private[mustache] sealed trait Component {
  def rendered(context: Context)(implicit global: Context): Either[Any, String]
  def formatted: String
}
private[mustache] trait Value extends Component
private[mustache] trait Container extends Component {
  def template: Template
}
private[mustache] trait ParserDirective extends Component {
  final def rendered(context: Context)(implicit global: Context) = Right("")
  def modified(implicit config: ParserConfig): ParserConfig
}

private[mustache] object Template {
  type Result = Either[Any, String]
  val emptyResult: Result = Right("")
}

private[mustache] case class Template(components: Component*) extends Component {
  def append(template: Template) = Template(components ++ template.components :_*)
  def rendered(context: Context)(implicit global: Context) = components.foldLeft(Template.emptyResult) {
    case (Right(acc), component) => component.rendered(global ++ context).right.map(acc + _)
    case (failure: Left[Any, String], _) => failure
  }

  lazy val formatted = components.foldLeft("") { case (acc, component) => acc + component.formatted }
}
