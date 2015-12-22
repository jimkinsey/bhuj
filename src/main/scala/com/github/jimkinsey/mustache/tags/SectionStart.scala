package com.github.jimkinsey.mustache.tags

import java.util.regex.Pattern

import com.github.jimkinsey.mustache.Renderer
import com.github.jimkinsey.mustache.Renderer._

object SectionStart extends Tag {
  type Lambda = (String, (String => Result)) => Result
  type Render = ((String, Context) => Result)
  case class UnclosedSection(name: String) extends Failure

  val pattern = """^#(.+)$""".r
  def process(name: String, context: Context, postTagTemplate: String, render: Render) = {
    ("""(?s)(.*?)\{\{/""" + Pattern.quote(name) + """\}\}(.*)""").r.findFirstMatchIn(postTagTemplate).map(m => (m.group(1), m.group(2))).flatMap {
      case (sectionTemplate, postSectionTemplate) =>
        context.get(name).collect {
          case true =>
            render(sectionTemplate, context)
          case nonFalseValue: Context =>
            render(sectionTemplate, nonFalseValue)
          case iterable: Renderer.ContextList if iterable.nonEmpty =>
            iterable.foldLeft[Result](Right("")) {
              case (Right(acc), item) => render(sectionTemplate, item).right.map(acc + _)
              case (fail, _) => fail
            }
          case lambda: Lambda =>
            lambda(sectionTemplate, render(_, context))
        }.orElse(Some(Right(""))).map(_.right.map(_ -> postSectionTemplate))
    }.getOrElse(Left(UnclosedSection(name)))
  }

}