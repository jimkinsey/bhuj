package bhuj.components

import bhuj.parsing.Delimiters

private[bhuj] case class Comment(content: String) extends Value {
  def formatted(delimiters: Delimiters) = delimiters.tag(s"!$content")
}
