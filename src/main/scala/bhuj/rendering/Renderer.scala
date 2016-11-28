package bhuj.rendering

import bhuj.Mustache.Templates
import bhuj.model.{Partial, _}
import bhuj.formatting.Formatter
import bhuj.{LambdaFailure, _}

private[bhuj] class Renderer(parse: ParseTemplate, templates: Templates) {

  def rendered(template: Template, context: Context)(implicit global: Context = emptyContext): Result = {
    template.components.foldLeft(emptyResult) {
      case (Right(acc), c)         => rendered(c, global ++ context).right.map(acc + _)
      case (failure: Left[_,_], _) => failure
    }
  }

  private def rendered(component: Component, context: Context): Result = component match {
    case Text(content)               => Right(content)
    case Variable(name)              => Right(context.get(name).map(_.toString).map(escapeHTML).getOrElse(""))
    case variable: UnescapedVariable => Right(context.get(variable.name).map(_.toString).getOrElse(""))
    case section: Section            => renderedSection(section, context)
    case section: InvertedSection    => renderedInvertedSection(section, context)
    case Partial(name)               => renderedPartial(name, context)
    case _                           => emptyResult
  }

  private def escapeHTML(str: String) = str.foldLeft("") { case (acc, char) => acc + escapeCode.getOrElse(char, char) }

  private lazy val escapeCode = Map(
    '<'  -> "&lt;",
    '>'  -> "&gt;",
    '&'  -> "&amp;",
    '\"' -> "&quot;",
    '''  -> "&#39;"
  )

  private def renderedPartial(name: String, context: Context) = {
    for {
      template <- templates(name).toRight({ TemplateNotFound(name) }).right
      parsed   <- parse(template).right
      result   <- rendered(parsed, context).right
    } yield { result }
  }

  private def renderedInvertedSection(section: InvertedSection, context: Context) = {
    context.get(section.name).map {
      case false                                     => rendered(section.template, context)(emptyContext)
      case None                                      => rendered(section.template, context)(emptyContext)
      case iterable: Iterable[_] if iterable.isEmpty => rendered(section.template, context)(emptyContext)
      case _                                         => emptyResult
    }.getOrElse(rendered(section.template, context))
  }

  private def renderedSection(section: Section, context: Context) = {
    def renderedLambda(lambda: Lambda) = {
      val render: NonContextualRender = (templateString) => for {
        template <- parse(templateString).right
        result   <- rendered(template, context).right
      } yield { result }
      lambda(formatter.source(section.template), render).left.map{ f: Any => LambdaFailure(section.name, f) }
    }

    def renderedIterable(iterable: Iterable[Context]) = iterable.foldLeft(emptyResult) {
      case (Right(acc), ctx) => rendered(section.template, ctx)(emptyContext).right.map(acc + _)
      case (Left(fail), _)   => Left(fail)
    }

    def renderedOption(option: Option[Any]) = option match {
      case Some(ctx: Context @unchecked) => rendered(section.template, ctx)(emptyContext)
      case Some(nonCtx)                  => rendered(section.template, Map("_" -> nonCtx))(emptyContext)
      case _                             => emptyResult
    }

    context.get(section.name).map {
      case true                                   => rendered(section.template, context)(emptyContext)
      case lambda: Lambda @unchecked              => renderedLambda(lambda)
      case map: Context @unchecked                => rendered(section.template, map)
      case iterable: Iterable[Context] @unchecked => renderedIterable(iterable)
      case option: Option[Any]                    => renderedOption(option)
      case _                                      => emptyResult
    }.getOrElse(emptyResult)
  }

  private lazy val formatter = new Formatter

}
