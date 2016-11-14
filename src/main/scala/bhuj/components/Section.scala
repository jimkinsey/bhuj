package bhuj.components

import bhuj._
import bhuj.components.Partial.RenderTemplate
import bhuj.parsing.Delimiters

private[bhuj] case class Section(name: String, template: Template, render: Render) extends Container {
  def formatted(delimiters: Delimiters) = s"${delimiters.tag(s"#$name")}${template.formatted(delimiters)}${delimiters.tag(s"/$name")}"
}

private[bhuj] case class InvertedSection(name: String, template: Template, render: RenderTemplate) extends Container {
  def formatted(delimiters: Delimiters) = s"${delimiters.tag(s"^$name")}${template.formatted(delimiters)}${delimiters.tag(s"/$name")}"
}
