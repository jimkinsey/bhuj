package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.TemplateParser.{Delimiters, TagParseFailure, UnrecognisedTag}
import com.github.jimkinsey.mustache.components.Text
import com.github.jimkinsey.mustache.rendering.{Component, Template}

import scala.util.matching.Regex

object TagParser {
  trait Failure
}

trait TagParser {
  def pattern: Regex
  def parsed(name: String): Either[TagParser.Failure, Component]
}

object TemplateParser {
  sealed trait Failure
  case class UnrecognisedTag(index: Int, content: String) extends Failure
  case class TagParseFailure(index: Int, failure: TagParser.Failure) extends Failure

  case class Delimiters(start: String, end: String)
}

private[mustache] class TemplateParser(
  delimiters: Delimiters = Delimiters("{{", "}}"),
  tagParsers: Seq[TagParser] = Seq.empty) {

  def parse(template: String): Either[TemplateParser.Failure, Template] =
    if (template.length > 0) {
      val tagIndex = template.indexOf(delimiters.start)
      if (tagIndex < 0) {
        Right(Template(Text(template)))
      }
      else {
        val endIndex = template.substring(tagIndex + delimiters.start.length).indexOf(delimiters.end)
        val preTag = template.substring(0, tagIndex)
        val postTag = template.substring(tagIndex + endIndex + delimiters.end.length + 2)
        val tagContent = template.substring(tagIndex + delimiters.start.length, tagIndex + endIndex + delimiters.end.length)
        tagParsers
          .collectFirst { case parser if parser.pattern.findFirstIn(tagContent).isDefined => {
            parser.parsed(tagContent).left.map(failure => TagParseFailure(tagIndex, failure))
          }}
          .getOrElse(Left(UnrecognisedTag(tagIndex, tagContent)))
          .right
          .flatMap(c =>
            parse(postTag)
              .right
              .map(rest => Template(Seq(Text(preTag), c).filterNot(_ == Text("")) ++ rest.components:_*))
              .left
              .map {
                case tpf: TagParseFailure => tpf.copy(index = tpf.index + endIndex + delimiters.end.length + 2)
                case ut: UnrecognisedTag => ut.copy(index = ut.index + endIndex + delimiters.end.length + 2)
              }
          )
      }
    }
    else
      Right(Template())
}
