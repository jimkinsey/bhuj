package bhuj.rendering

import bhuj.components.{Partial, _}
import bhuj.formatting.Formatter
import bhuj.{LambdaFailure, _}

private[bhuj] class Renderer {

  def rendered(template: Template, context: Context)(implicit global: Context = emptyContext): Result = {
    template.components.foldLeft(emptyResult) {
      case (Right(acc), c) => rendered(c, global ++ context).right.map(acc + _)
      case (failure: Left[Failure, String], _) => failure
    }
  }

  private[bhuj] def rendered(component: Component, context: Context): Result = component match {
    case Text(content) => Right(content)
    case Variable(name) => Right(context.get(name).map(_.toString).map(escapeHTML).getOrElse(""))
    case variable: UnescapedVariable => Right(context.get(variable.name).map(_.toString).getOrElse(""))
    case section: Section => renderedSection(section, context)
    case section: InvertedSection => renderedInvertedSection(section, context)
    case Partial(name, render) => render(name, context)
    case _ => emptyResult
  }

  private def escapeHTML(str: String) = str.foldLeft("") { case (acc, char) => acc + escapeCode.getOrElse(char, char) }

  private lazy val escapeCode = Map(
    '<' -> "&lt;",
    '>' -> "&gt;",
    '&' -> "&amp;",
    '\"' -> "&quot;",
    ''' -> "&#39;"
  )

  private def renderedInvertedSection(section: InvertedSection, context: Context) = {
    context.get(section.name).map {
      case false => rendered(section.template, context)(emptyContext)
      case None => rendered(section.template, context)(emptyContext)
      case iterable: Iterable[Context]@unchecked if iterable.isEmpty => rendered(section.template, context)(emptyContext)
      case _ => emptyResult
    }.getOrElse(rendered(section.template, context))
  }

  private def renderedSection(section: Section, context: Context) = {
    context.get(section.name).map {
      case true => rendered(section.template, context)(emptyContext)
      case lambda: Lambda @unchecked =>
        lambda(formatter.source(section.template), section.render(_, context)).left.map{ f: Any => LambdaFailure(section.name, f) }
      case map: Context @unchecked => rendered(section.template, map)
      case iterable: Iterable[Context] @unchecked => iterable.foldLeft(emptyResult) {
        case (Right(acc), ctx) => rendered(section.template, ctx)(emptyContext).right.map(acc + _)
        case (Left(fail), _) => Left(fail)
      }
      case Some(item) => item match {
        case ctx: Context @unchecked => rendered(section.template, ctx)(emptyContext)
        case nonCtx => rendered(section.template, Map("_" -> nonCtx))(emptyContext)
      }
      case _ => emptyResult
    }.getOrElse(emptyResult)
  }

  private lazy val formatter = new Formatter

}
