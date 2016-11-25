package bhuj.rendering

import bhuj.Mustache.{Templates, emptyTemplates}
import bhuj._
import bhuj.formatting.Formatter
import bhuj.model._

private[bhuj] class Renderer(
  scalaConverter: ScalaConverter,
  compiler: TemplateCompiler,
  parse: ParseTemplate,
  templates: Templates = emptyTemplates) {

  def rendered(template: Template, context: Context)(implicit global: Context = emptyContext): Result = {
    for {
      preRenderedPartials <- renderedPartials(template, global ++ context).right
      preRenderedSections <- renderedSections(template, global ++ context).right
      scala               <- scalaConverter.scala(template).right
      render              <- compiler.compiled(scala).right
      result              <- render(new Tools(preRenderedPartials, preRenderedSections))(global ++ context).right
    } yield {
      result
    }
  }

  private def renderedPartials(template: Template, context: Context)(implicit global: Context = emptyContext): Either[Failure, Map[String, String]] = {
    /* TODO possible optimisation is to convert partials which do not reference other partials into lists of components
     maybe could even check if there is recursion / recursively optimise templates? */
    template.components.foldLeft[Either[Failure, Map[String, String]]](Right(Map.empty)) {
      case (Right(acc), Partial(name)) =>
        for {
          rawPartial      <- templates(name).toRight({ TemplateNotFound(name) }).right
          partial         <- parse(rawPartial).right
          renderedPartial <- rendered(partial, context).right
        } yield {
          acc ++ Map(name -> renderedPartial)
        }
      case (f: Left[Failure, _], _) => f
      case (acc, _) => acc
    }
  }

  private def renderedInvertedSection(section: InvertedSection, context: Context)(implicit global: Context = emptyContext) = {
    context.get(section.name).map {
      case false => rendered(section.template, context)
      case None => rendered(section.template, context)
      case iterable: Iterable[Context]@unchecked if iterable.isEmpty => rendered(section.template, context)
      case _ => emptyResult
    }.getOrElse(rendered(section.template, context))
  }


  private def renderedSections(template: Template, context: Context)(implicit global: Context = emptyContext) = {
    def key(component: Component): Int = component.hashCode()
    template.components.foldLeft[Either[Failure, Map[Int, String]]](Right(Map.empty)) {
      case (Right(acc), section: Section) if !acc.contains(key(section)) =>
        renderedSection(section, context).right.map(rendered => acc ++ Map(key(section) -> rendered))
      case (Right(acc), section: InvertedSection) if !acc.contains(key(section)) =>
        renderedInvertedSection(section, context).right.map(rendered => acc ++ Map(key(section) -> rendered))
      case (failure: Left[_,_], _) => failure
      case (acc, _) => acc
    }
  }

  private def renderedSection(section: Section, context: Context)(implicit global: Context = emptyContext) = {
    context.get(section.name).map {
      case true => rendered(section.template, context)
      case lambda: Lambda @unchecked =>
        val render: NonContextualRender = (templateString) => for {
          template <- parse(templateString).right
          result <- rendered(template, context).right
        } yield { result }
        lambda(formatter.source(section.template), render).left.map{ f: Any => LambdaFailure(section.name, f) }
      case map: Context @unchecked => rendered(section.template, map)
      case iterable: Iterable[Context] @unchecked => iterable.foldLeft(emptyResult) {
        case (Right(acc), ctx) => rendered(section.template, ctx).right.map(acc + _)
        case (Left(fail), _) => Left(fail)
      }
      case Some(item) => item match {
        case ctx: Context @unchecked => rendered(section.template, ctx)
        case nonCtx => rendered(section.template, Map("_" -> nonCtx))
      }
      case _ => emptyResult
    }.getOrElse(emptyResult)
  }

  private lazy val formatter = new Formatter

}
