package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.TemplateParser.{InvalidParserPattern, Delimiters, TagParseFailure, UnrecognisedTag}
import com.github.jimkinsey.mustache.components.Text
import com.github.jimkinsey.mustache.rendering.{Component, Template}

import scala.util.matching.Regex
import scala.util.matching.Regex.Match

object TagParser {
  trait Failure
}

trait TagParser {
  def pattern: Regex
  def parsed(name: String): Either[Any, Component]
}

object TemplateParser {
  sealed trait Failure
  case class UnrecognisedTag(index: Int) extends Failure
  case class TagParseFailure(index: Int, failure: Any) extends Failure
  case class InvalidParserPattern(pattern: Regex) extends Failure

  case class Delimiters(start: String, end: String) {
    val combinedLength = start.length + end.length
  }

}

private[mustache] class TemplateParser(
  delimiters: Delimiters = Delimiters("{{", "}}"),
  tagParsers: Seq[TagParser] = Seq.empty) {

  def parse(template: String): Either[TemplateParser.Failure, Template] = {
    def parse(unparsed: String, index: Int): Either[TemplateParser.Failure, Template] = {
      unparsed.indexOf(delimiters.start) match {
        case _ if unparsed.length == 0 => Right(Template())
        case i if i < 0 => Right(Template(Text(unparsed)))
        case i if i > 0 => {
          lazy val head = Template(Text(unparsed.substring(0, i)))
          for {
            tail <- parse(unparsed.substring(i), index + i).right
          } yield head.append(tail)
        }
        case 0 => {
          def delimited(regex: Regex) = s"${Regex.quote(delimiters.start)}${regex.pattern}${Regex.quote(delimiters.end)}".r
          def matchingParser = tagParsers.find(tp => delimited(tp.pattern).findPrefixOf(unparsed).isDefined).toRight({ UnrecognisedTag(index) })
          def tagMatch(parser: TagParser) = delimited(parser.pattern).findPrefixMatchOf(unparsed).filter(_.groupCount == 1).toRight({ InvalidParserPattern(parser.pattern) })
          def component(parser: TagParser, tagContent: String) = parser.parsed(tagContent).left.map(f => TagParseFailure(index, f))

          for {
            parser <- matchingParser.right
            tagMatch <- tagMatch(parser).right
            head <- component(parser, tagMatch.group(1)).right
            tail <- parse(tagMatch.after.toString, index + tagMatch.end).right
          } yield Template(head).append(tail)
        }
      }
    }
    parse(template, 0)
  }

}
