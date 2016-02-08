package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.Context
import com.github.jimkinsey.mustache.components.Section.{Lambda, emptyResult}

object Section {
  type Render = (Template, Context) => Either[Any,String]
  type NonContextualRender = (Template) => Either[Any,String]
  type Lambda = (Template, NonContextualRender) => Either[Any,String]
  val emptyResult: Either[Any,String] = Right("")
}

case class Section(name: String, template: Template) extends Container {
  override def rendered(context: Context)(implicit global: Context): Either[Any, String] = {
    context.get(name).map {
      case true => template.rendered(context)
      case lambda: Lambda @unchecked => lambda(template, _.rendered(context))
      case map: Context @unchecked => template.rendered(map)
      case iterable: Iterable[Context] @unchecked => iterable.foldLeft(emptyResult) {
        case (Right(acc), ctx) => template.rendered(ctx).right.map(acc + _)
        case (Left(fail), _) => Left(fail)
      }
      case Some(ctx: Context) => template.rendered(ctx)
      case _ => emptyResult
    }.getOrElse(emptyResult)
  }
}

case class InvertedSection(name: String, template: Template) extends Container {
  override def rendered(context: Context)(implicit global: Context): Either[Any, String] = {
    context.get(name).map {
      case false => template.rendered(context)
      case None => template.rendered(context)
      case iterable: Iterable[Context] @unchecked if iterable.isEmpty => template.rendered(context)
      case _ => emptyResult
    }.getOrElse(template.rendered(context))
  }
}
