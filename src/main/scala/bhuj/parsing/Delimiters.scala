package bhuj.parsing

import scala.util.matching.Regex.quote

private[bhuj] case class Delimiters(start: String, end: String) {
  def pattern(innerPattern: String): String = s"""${quote(start)}$innerPattern${quote(end)}"""
  def tag(content: String) = s"""$start$content$end"""
}
