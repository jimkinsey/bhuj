package bhuj.rendering

import bhuj.Mustache.Templates
import bhuj.formatting.Formatter
import bhuj.model.{Partial, _}
import bhuj.{LambdaFailure, _}

import scala.concurrent.{ExecutionContext, Future}

private[bhuj] class Renderer(parse: ParseTemplate, templates: Templates) {
  import bhuj.result.EventualResult._

  def rendered(template: Template, context: Context)(implicit global: Context = emptyContext, ec: ExecutionContext): Future[Result] = {
    val renderingComponents = template.components.map(rendered(_, global ++ context)).to[collection.immutable.Seq]
    Future.foldLeft(renderingComponents)(emptyResult) {
      case (failure: Left[_,_], _)  => failure
      case (_, failure: Left[_,_])  => failure
      case (Right(acc), Right(res)) => Right(acc + res)
    }
  }

  private def rendered(component: Component, context: Context)(implicit executionContext: ExecutionContext): Future[Result] = component match {
    case Text(content)               => Future successful Right(content)
    case Variable(name)              => Future successful Right(context.get(name).map(_.toString).map(escapeHTML).getOrElse(""))
    case variable: UnescapedVariable => Future successful Right(context.get(variable.name).map(_.toString).getOrElse(""))
    case section: Section            => renderedSection(section, context)
    case section: InvertedSection    => renderedInvertedSection(section, context)
    case Partial(name)               => renderedPartial(name, context)
    case _                           => Future successful emptyResult
  }

  private def escapeHTML(str: String) = str.foldLeft("") { case (acc, char) => acc + escapeCode.getOrElse(char, char) }

  private lazy val escapeCode = Map(
    '<'  -> "&lt;",
    '>'  -> "&gt;",
    '&'  -> "&amp;",
    '\"' -> "&quot;",
    '''  -> "&#39;"
  )

  private def renderedPartial(name: String, context: Context)(implicit executionContext: ExecutionContext): Future[Result] = {
    for {
      template <- templates(name)           |> fromFutureOption({ TemplateNotFound(name) })
      parsed   <- parse(template)           |> fromEither
      result   <- rendered(parsed, context) |> fromFutureEither
    } yield { result }
  }

  private def renderedInvertedSection(section: InvertedSection, context: Context)(implicit executionContext: ExecutionContext): Future[Result] = {
    context.get(section.name).map {
      case false                                     => rendered(section.template, context)(emptyContext, executionContext)
      case None                                      => rendered(section.template, context)(emptyContext, executionContext)
      case iterable: Iterable[_] if iterable.isEmpty => rendered(section.template, context)(emptyContext, executionContext)
      case _                                         => Future successful emptyResult
    } getOrElse rendered(section.template, context)
  }

  private def renderedSection(section: Section, context: Context)(implicit executionContext: ExecutionContext): Future[Result] = {
    def renderedLambda(lambda: Lambda): Future[Result] = {
      val render: NonContextualRender = (templateString) => {
        for {
          template <- parse(templateString)       |> fromEither
          result   <- rendered(template, context) |> fromFutureEither
        } yield { result }
      }
      lambda(formatter.source(section.template), render) map (_.left.map{ f: Any => LambdaFailure(section.name, f) })
    }

    def renderedIterable(iterable: Iterable[Context]): Future[Result] = {
      val renderingSections = iterable.map(rendered(section.template, _)(emptyContext, executionContext)).to[collection.immutable.Iterable]
      Future.foldLeft(renderingSections)(emptyResult) {
        case (Left(fail), _)          => Left(fail)
        case (_, Left(fail))          => Left(fail)
        case (Right(acc), Right(res)) => Right(acc + res)
      }
    }

    def renderedOption(option: Option[Any]): Future[Result] = option match {
      case Some(ctx: Context @unchecked) => rendered(section.template, ctx)(emptyContext, executionContext)
      case Some(nonCtx)                  => rendered(section.template, Map("_" -> nonCtx))(emptyContext, executionContext)
      case _                             => Future successful emptyResult
    }

    context.get(section.name).map {
      case true                                   => rendered(section.template, context)(emptyContext, executionContext)
      case lambda: Lambda @unchecked              => renderedLambda(lambda)
      case map: Context @unchecked                => rendered(section.template, map)
      case iterable: Iterable[Context] @unchecked => renderedIterable(iterable)
      case option: Option[Any]                    => renderedOption(option)
      case _                                      => Future successful emptyResult
    }.getOrElse(Future successful emptyResult)
  }

  private lazy val formatter = new Formatter

}
