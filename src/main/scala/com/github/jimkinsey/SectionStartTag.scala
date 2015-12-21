package com.github.jimkinsey

import java.util.regex.Pattern

import com.github.jimkinsey.Renderer.{Failure, Result, Tag}

object SectionStartTag extends Tag {
  type Lambda = (String, (String => Result)) => Result

  val pattern = """^#(.+)$""".r
  def process(name: String, context: Renderer.Context, postTagTemplate: String, render: ((String, Renderer.Context) => Renderer.Result)) = {
    ("""(?s)(.*?)\{\{/""" + Pattern.quote(name) + """\}\}(.*)""").r.findFirstMatchIn(postTagTemplate).map(m => (m.group(1), m.group(2))).flatMap {
      case (sectionTemplate, postSectionTemplate) =>
        context.get(name).collect {
          case nonFalseValue: Renderer.Context =>
            render(sectionTemplate, nonFalseValue)
          case iterable: Renderer.ContextList if iterable.nonEmpty =>
            iterable.foldLeft[Renderer.Result](Right("")) {
              case (Right(acc), item) => render(sectionTemplate, item).right.map(acc + _)
              case (fail, _) => fail
            }
          case lambda: Lambda =>
            lambda(sectionTemplate, render(_, context))
        }.orElse(Some(Right(""))).map(_ -> postSectionTemplate)
    }.getOrElse((Left(UnclosedSection(name)), ""))
  }
  case class UnclosedSection(name: String) extends Failure
}
