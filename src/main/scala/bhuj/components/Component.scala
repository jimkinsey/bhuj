package bhuj.components

import bhuj.parsing.{Delimiters, ParserConfig}
import bhuj.{Result, doubleMustaches}

private[bhuj] sealed trait Component {
  def formatted(delimiters: Delimiters): String
}
private[bhuj] trait Value extends Component
private[bhuj] trait Container extends Component {
  def template: Template
}
private[bhuj] trait ParserDirective extends Component {
  def modified(implicit config: ParserConfig): ParserConfig
}

private[bhuj] object Template {
  def apply(components: Component*): Template = {
    this(doubleMustaches, components:_*)
  }
}

private[bhuj] case class Template(initialDelimiters: Delimiters, components: Component*) extends Component {

  lazy val source = formatted(initialDelimiters)

  def formatted(delimiters: Delimiters) = components.foldLeft(("", delimiters)) {
    case ((acc, previousDelimiters), component: SetDelimiters) => (acc + component.formatted(previousDelimiters), component.delimiters)
    case ((acc, currentDelimiters), component) => (acc + component.formatted(currentDelimiters), currentDelimiters)
  }._1
}
