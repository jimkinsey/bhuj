package bhuj.components

import bhuj.Template
import bhuj.parsing.{Delimiters, ParserConfig}

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
