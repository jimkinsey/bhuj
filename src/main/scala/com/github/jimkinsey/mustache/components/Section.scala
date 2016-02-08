package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.components.Section.{Lambda, Render, emptyResult}
import com.github.jimkinsey.mustache.rendering.Renderer.Context
import com.github.jimkinsey.mustache.rendering.{Container, Template}

object Section {
  type Render = (Template, Context) => Either[Any,String]
  type NonContextualRender = (Template) => Either[Any,String]
  type Lambda = (Template, NonContextualRender) => Either[Any,String]
  private val emptyResult: Either[Any,String] = Right("")
}

case class Section(name: String, template: Template) extends Container {
  def rendered(context: Context, render: Render): Either[Any, String] = {
    context.get(name).map {
      case true => render(template, context)
      case lambda: Lambda @unchecked => lambda(template, render(_, context))
      case map: Context @unchecked => render(template, map)
      case iterable: Iterable[Context] @unchecked => iterable.foldLeft(emptyResult) {
        case (Right(acc), ctx) => render(template, ctx).right.map(acc + _)
        case (Left(fail), _) => Left(fail)
      }
      case _ => emptyResult
    }.getOrElse(emptyResult)
  }
}
