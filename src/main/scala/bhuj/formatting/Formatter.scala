package bhuj.formatting

import bhuj.Template
import bhuj.components._
import bhuj.parsing.Delimiters

private[bhuj] class Formatter {

  def source(template: Template): String = source(template, template.initialDelimiters)

  private def source(template: Template, delimiters: Delimiters): String = template.components.foldLeft(("", delimiters)) {
    case ((acc, previousDelimiters), component: SetDelimiters) => (acc + formatted(component, previousDelimiters), component.delimiters)
    case ((acc, currentDelimiters), component) => (acc + formatted(component, currentDelimiters), currentDelimiters)
  }._1

  private def formatted(component: Component, delimiters: Delimiters): String = component match {
    case Text(content) => content
    case Variable(name) => delimiters.tag(name)
    case TripleDelimitedVariable(name) => delimiters.tag(s"{$name}")
    case AmpersandPrefixedVariable(name) => delimiters.tag(s"&$name")
    case SetDelimiters(newDelimiters) => delimiters.tag(s"=${newDelimiters.start} ${newDelimiters.end}=")
    case Section(name, template, _) => s"${delimiters.tag(s"#$name")}${source(template, delimiters)}${delimiters.tag(s"/$name")}"
    case InvertedSection(name, template, _) => s"${delimiters.tag(s"^$name")}${source(template, delimiters)}${delimiters.tag(s"/$name")}"
    case Partial(name, _) => delimiters.tag(s"> $name")
    case Comment(content) => delimiters.tag(s"!$content")
  }

}
