package com.github.jimkinsey.mustache.parsing

import scala.util.matching.Regex.quote

case class Delimiters(start: String, end: String) {
  def pattern(innerPattern: String): String = s"""${quote(start)}$innerPattern${quote(end)}"""
  def tag(content: String) = s"""$start$content$end"""
}

case class InvalidDelimiters(start: String, end: String)