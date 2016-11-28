package bhuj.model

import bhuj._
import bhuj.parsing.Delimiters

private[bhuj] case class Template(initialDelimiters: Delimiters, components: Seq[Component])

private[bhuj] object Template {
  def apply(components: Component*): Template = {
    this(doubleMustaches, components)
  }
}