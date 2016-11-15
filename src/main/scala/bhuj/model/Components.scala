package bhuj.model

import bhuj.parsing.Delimiters

private[bhuj] sealed trait Component
private[bhuj] trait Container { self: Component =>
  def template: Template
}
private[bhuj] trait UnescapedVariable { self: Component =>
  def name: String
}

private[bhuj] case class Comment(content: String) extends Component
private[bhuj] case class Partial(name: String) extends Component
private[bhuj] case class Section(name: String, template: Template) extends Component with Container
private[bhuj] case class InvertedSection(name: String, template: Template) extends Component with Container
private[bhuj] case class SetDelimiters(delimiters: Delimiters) extends Component
private[bhuj] case class Text(content: String) extends Component
private[bhuj] case class TripleDelimitedVariable(name: String) extends Component with UnescapedVariable
private[bhuj] case class AmpersandPrefixedVariable(name: String) extends Component with UnescapedVariable
private[bhuj] case class Variable(name: String) extends Component