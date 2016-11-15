package bhuj.components

import bhuj._
import bhuj.parsing.Delimiters

private[bhuj] sealed trait Component
private[bhuj] trait Value { self: Component =>  }
private[bhuj] trait Container { self: Component =>
  def template: Template
}
private[bhuj] trait ParserDirective { self: Component => }
private[bhuj] trait UnescapedVariable { self: Component with Value =>
  def name: String
}

private[bhuj] case class Comment(content: String) extends Component with Value
private[bhuj] case class Partial(name: String, render: Render) extends Component with Value
private[bhuj] case class Section(name: String, template: Template, render: Render) extends Component with Container
private[bhuj] case class InvertedSection(name: String, template: Template, render: Render) extends Component with Container
private[bhuj] case class SetDelimiters(delimiters: Delimiters) extends Component with ParserDirective
private[bhuj] case class Text(content: String) extends Component with Value
private[bhuj] case class TripleDelimitedVariable(name: String) extends Component with Value with UnescapedVariable
private[bhuj] case class AmpersandPrefixedVariable(name: String) extends Component with Value with UnescapedVariable
private[bhuj] case class Variable(name: String) extends Component with Value