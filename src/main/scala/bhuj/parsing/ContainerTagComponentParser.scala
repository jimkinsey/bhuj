package bhuj.parsing

import bhuj.model._
import bhuj.{ParseTemplateFailure, Failure, Render, UnclosedTag}

import scala.util.matching.Regex.quote

private[bhuj] sealed trait ContainerTagComponentParser[+T <: Component with Container] extends ComponentParser[T] {
  def prefix: String
  def constructor: (String, Template) => T

  final def parseResult(template: String)(implicit parserConfig: ParserConfig): Either[ParseTemplateFailure, Option[ParseResult[T]]] = {
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
                component = constructor(mtch.group(1), template),
                remainder = afterOpenTag.substring(i + parserConfig.delimiters.tag(s"/$key").length)
              ))
            }
      }
    }
  }

  private def indexOfClosingTag(key: String, template: String)(implicit parserConfig: ParserConfig): Int = {
    parserConfig.delimiters.pattern(s"""([#^/]${quote(key)})""").r.findAllMatchIn(template).foldLeft[Either[Int, Int]](Left(0)) {
      case (Left(0), m) if m.group(1).startsWith("/")     => Right(m.start)
      case (Left(open), m) if m.group(1).startsWith("/")  => Left(open - 1)
      case (Left(open), m) if !m.group(1).startsWith("/") => Left(open + 1)
      case (Right(index), _)                              => Right(index)
    }.right.toOption.getOrElse(-1)
  }
}

private[bhuj] object SectionParser extends ContainerTagComponentParser[Section] {
  lazy val prefix = "#"
  lazy val constructor = Section.apply _
}

private[bhuj] object InvertedSectionParser extends ContainerTagComponentParser[InvertedSection] {
  lazy val prefix = "^"
  lazy val constructor = InvertedSection.apply _
}