package bhuj.components

import bhuj.parsing.{Delimiters, ParserConfig}

private[bhuj] case class SetDelimiters(delimiters: Delimiters) extends ParserDirective {
  def modified(implicit config: ParserConfig): ParserConfig = config.copy(delimiters = delimiters)
  def formatted(formatDelimiters: Delimiters) = formatDelimiters.tag(s"=${delimiters.start} ${delimiters.end}=")
}