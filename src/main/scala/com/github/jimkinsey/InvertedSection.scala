package com.github.jimkinsey

import java.util.regex.Pattern

import com.github.jimkinsey.Renderer.{Context, Failure, Result, Tag}

import scala.util.matching.Regex

object InvertedSection extends Tag {
  case class UnclosedInvertedSection(name: String) extends Failure

  val pattern: Regex = """^\^(.+)$""".r

  def process(name: String, context: Context, postTagTemplate: String, render: (String, Context) => Result): Either[Failure, (String, String)] = {
    ("""(?s)(.*?)\{\{/""" + Pattern.quote(name) + """\}\}(.*)""").r
      .findFirstMatchIn(postTagTemplate)
      .map(m =>
        if(!context.keySet.contains(name) || isEmptyIterable(context, name) || (context(name) == false))
          render(m.group(1), context).right.map(_ -> m.group(2))
        else
          Right("" -> m.group(2))
      )
      .getOrElse(Left(UnclosedInvertedSection(name)))
  }

  private def isEmptyIterable(context: Context, name: String) = context(name) match {
    case iterable: Iterable[_] => iterable.isEmpty
    case _ => false
  }

}
