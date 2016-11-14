package bhuj

import bhuj.components.{Component, SetDelimiters}
import bhuj.parsing.Delimiters

private[bhuj] case class Template(initialDelimiters: Delimiters, components: Component*) {

  lazy val source = formatted(initialDelimiters)

  def formatted(delimiters: Delimiters) = components.foldLeft(("", delimiters)) {
    case ((acc, previousDelimiters), component: SetDelimiters) => (acc + component.formatted(previousDelimiters), component.delimiters)
    case ((acc, currentDelimiters), component) => (acc + component.formatted(currentDelimiters), currentDelimiters)
  }._1
}

private[bhuj] object Template {
  def apply(components: Component*): Template = {
    this(doubleMustaches, components:_*)
  }
}