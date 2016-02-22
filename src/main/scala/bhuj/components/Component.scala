package bhuj.components

import bhuj.parsing.{Delimiters, ParserConfig}
import bhuj.{Failure, Result, Context, doubleMustaches}

private[bhuj] sealed trait Component {
  def rendered(context: Context)(implicit global: Context): Result
  def formatted(delimiters: Delimiters): String
}
private[bhuj] trait Value extends Component
private[bhuj] trait Container extends Component {
  def template: Template
}
private[bhuj] trait ParserDirective extends Component {
  final def rendered(context: Context)(implicit global: Context) = Right("")
  def modified(implicit config: ParserConfig): ParserConfig
}

private[bhuj] object Template {
  val emptyResult: Result = Right("")

  def apply(components: Component*): Template = {
    this(doubleMustaches, components:_*)
  }
}

private[bhuj] case class Template(initialDelimiters: Delimiters, components: Component*) extends Component {

  def rendered(context: Context)(implicit global: Context): Result = components.foldLeft(Template.emptyResult) {
    case (Right(acc), component) => component.rendered(global ++ context).right.map(acc + _)
    case (failure: Left[Failure, String], _) => failure
  }

  lazy val source = formatted(initialDelimiters)

  def formatted(delimiters: Delimiters) = components.foldLeft(("", delimiters)) {
    case ((acc, previousDelimiters), component: SetDelimiters) => (acc + component.formatted(previousDelimiters), component.delimiters)
    case ((acc, currentDelimiters), component) => (acc + component.formatted(currentDelimiters), currentDelimiters)
  }._1
}
