package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.components.{InvertedSection, Section, Template, Container}
import com.github.jimkinsey.mustache.parsing.ContainerTagComponentParser.UnclosedTag
import scala.util.matching.Regex.quote

object ContainerTagComponentParser {
  case object UnclosedTag
}

trait ContainerTagComponentParser[+T <: Container] extends ComponentParser[T] {
  def prefix: String
  def constructor: (String, Template) => T

  final def parseResult(template: String)(implicit parserConfig: ParserConfig): Either[Any, Option[ParseResult[T]]] = {
    s"""\\{\\{${quote(prefix)}(.+?)\\}\\}""".r.findPrefixMatchOf(template) match {
      case None => Right(None)
      case Some(mtch) =>
        val key = mtch.group(1)
        val afterOpenTag = mtch.after.toString
        indexOfClosingTag(key, afterOpenTag) match {
          case i if i < 0 => Left(UnclosedTag)
          case i =>
            parserConfig.parsed(afterOpenTag.substring(0, i)).right.map { template =>
              Some(ParseResult(constructor(mtch.group(1), template), afterOpenTag.substring(i + s"{{/$key}}".length)))
            }
      }
    }
  }

  private def indexOfClosingTag(key: String, template: String): Int = {
    s"""\\{\\{(.${quote(key)})\\}\\}""".r.findAllMatchIn(template).foldLeft[Either[Int, Int]](Left(0)) {
      case (Left(0), m) if m.group(1).startsWith("/")     => Right(m.start)
      case (Left(open), m) if m.group(1).startsWith("/")  => Left(open - 1)
      case (Left(open), m) if !m.group(1).startsWith("/") => Left(open + 1)
      case (Right(index), _)                              => Right(index)
    }.right.toOption.getOrElse(-1)
  }
}

object SectionParser extends ContainerTagComponentParser[Section] {
  lazy val prefix = "#"
  lazy val constructor = Section.apply _
}

object InvertedSectionParser extends ContainerTagComponentParser[InvertedSection] {
  lazy val prefix = "^"
  lazy val constructor = InvertedSection.apply _
}