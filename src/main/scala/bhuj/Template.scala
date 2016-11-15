package bhuj

import bhuj.components.Component
import bhuj.parsing.Delimiters

private[bhuj] case class Template(initialDelimiters: Delimiters, components: Component*)

private[bhuj] object Template {
  def apply(components: Component*): Template = {
    this(doubleMustaches, components:_*)
  }
}