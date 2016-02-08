package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.TemplateParser._
import com.github.jimkinsey.mustache.components.{Template, Text}
import com.github.jimkinsey.mustache.parsing.{ContainerTagParser, ValueTagParser, TagParser}

import scala.util.matching.Regex

private[mustache] object TemplateParser {
  sealed trait Failure
  case class UnrecognisedTag(index: Int) extends Failure
  case class TagParseFailure(index: Int, failure: Any) extends Failure
  case class InvalidParserPattern(pattern: Regex) extends Failure
  case class UnclosedTag(index: Int) extends Failure

  case class Delimiters(start: String, end: String) {
    val combinedLength = start.length + end.length
  }
}

private[mustache] class TemplateParser(
  delimiters: Delimiters = Delimiters("{{", "}}"),
  tagParsers: Seq[TagParser] = Seq.empty) {

  def parse(template: String): Either[TemplateParser.Failure, Template] = {
    def parsedTemplate(unparsed: String, index: Int): Either[TemplateParser.Failure, Template] = {
      unparsed.indexOf(delimiters.start) match {
        case _ if unparsed.length == 0 => Right(Template())
        case i if i < 0 => Right(Template(Text(unparsed)))
        case i if i > 0 => {
          lazy val head = Template(Text(unparsed.substring(0, i)))
          for {
            tail <- parsedTemplate(unparsed.substring(i), index + i).right
          } yield head.append(tail)
        }
        case 0 => {
          def delimited(regex: Regex) = s"${Regex.quote(delimiters.start)}${regex.pattern}${Regex.quote(delimiters.end)}".r
          def matchingParser = tagParsers.find(tp => delimited(tp.pattern).findPrefixOf(unparsed).isDefined).toRight({ UnrecognisedTag(index) })
          def tagMatch(parser: TagParser, index: Int): Either[Failure, TagMatch] = {
            delimited(parser.pattern)
              .findPrefixMatchOf(unparsed)
              .filter(_.groupCount == 1)
              .toRight({ InvalidParserPattern(parser.pattern) })
              .right
              .flatMap { m => parser match {
                case parser: ValueTagParser => Right(ValueTagMatch(m.group(1), m.after.toString, m.end, parser))
                case parser: ContainerTagParser => m.after.toString.indexOf(s"${delimiters.start}/${m.group(1)}${delimiters.end}") match {
                  case i if i < 0 => Left(UnclosedTag(index))
                  case i =>
                    val remainderIndex = i + delimiters.combinedLength + m.group(1).length + 1
                    Right(ContainerTagMatch(
                      name = m.group(1),
                      remainder = m.after.toString.substring(remainderIndex),
                      remainderIndex = remainderIndex,
                      content = m.after.toString.substring(0, i),
                      parser = parser
                    ))
                }
              }
            }
          }
          def component(tagMatch: TagMatch) = tagMatch match {
            case ValueTagMatch(name, _, _, parser) => parser.parsed(name).left.map(f => TagParseFailure(index, f))
            case ContainerTagMatch(name, _, _, content, parser) => for {
              template <- parse(content).right
              component <- parser.parsed(name, template).left.map(f => TagParseFailure(index, f)).right
            } yield component
          }

          for {
            parser <- matchingParser.right
            tagMatch <- tagMatch(parser, index).right
            head <- component(tagMatch).right
            tail <- parsedTemplate(tagMatch.remainder, index + tagMatch.remainderIndex).right
          } yield Template(head).append(tail)
        }
      }
    }
    parsedTemplate(template, 0)
  }

  private sealed trait TagMatch {
    def name: String
    def remainder: String
    def remainderIndex: Int
    def parser: TagParser
  }
  private case class ValueTagMatch(
    name: String,
    remainder: String,
    remainderIndex: Int,
    parser: ValueTagParser) extends TagMatch
  private case class ContainerTagMatch(
    name: String,
    remainder: String,
    remainderIndex: Int,
    content: String,
    parser: ContainerTagParser) extends TagMatch
}
