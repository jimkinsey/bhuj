package bhuj.components

import bhuj.Context
import bhuj.parsing.Delimiters

private[bhuj] case class Comment(content: String) extends Value {
  def rendered(context: Context)(implicit global: Context) = Right("")
  def formatted(delimiters: Delimiters) = delimiters.tag(s"!$content")
}
