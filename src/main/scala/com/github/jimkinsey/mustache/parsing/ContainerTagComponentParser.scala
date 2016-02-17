package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.{Failure, UnclosedTag}
import com.github.jimkinsey.mustache.components.Partial.RenderTemplate
import com.github.jimkinsey.mustache.components.{Container, InvertedSection, Section, Template}

import scala.util.matching.Regex.quote

private[mustache] trait ContainerTagComponentParser[+T <: Container] extends ComponentParser[T] {
  def prefix: String
  def constructor: (String, Template, RenderTemplate) => T

  final def parseResult(template: String)(implicit parserConfig: ParserConfig): Either[Failure, Option[ParseResult[T]]] = {
    parserConfig.delimiters.pattern(s"""${quote(prefix)}(.+?)""").r.findPrefixMatchOf(template) match {
      case None => Right(None)
      case Some(mtch) =>
        val key = mtch.group(1)
        val afterOpenTag = mtch.after.toString
        indexOfClosingTag(key, afterOpenTag) match {
          case i if i < 0 => Left(UnclosedTag(key))
          case i =>
            parserConfig.parsed(afterOpenTag.substring(0, i)).right.map { template =>
              Some(ParseResult(
                component = constructor(mtch.group(1), template, parserConfig.rendered),
                remainder = afterOpenTag.substring(i + parserConfig.delimiters.tag(s"/$key").length)
              ))
            }
      }
    }
  }

  private def indexOfClosingTag(key: String, template: String)(implicit parserConfig: ParserConfig): Int = {
    parserConfig.delimiters.pattern(s"""(.${quote(key)})""").r.findAllMatchIn(template).foldLeft[Either[Int, Int]](Left(0)) {
      case (Left(0), m) if m.group(1).startsWith("/")     => Right(m.start)
      case (Left(open), m) if m.group(1).startsWith("/")  => Left(open - 1)
      case (Left(open), m) if !m.group(1).startsWith("/") => Left(open + 1)
      case (Right(index), _)                              => Right(index)
    }.right.toOption.getOrElse(-1)
  }
}

private[mustache] object SectionParser extends ContainerTagComponentParser[Section] {
  lazy val prefix = "#"
  lazy val constructor = Section.apply _
}

private[mustache] object InvertedSectionParser extends ContainerTagComponentParser[InvertedSection] {
  lazy val prefix = "^"
  lazy val constructor = InvertedSection.apply _
}