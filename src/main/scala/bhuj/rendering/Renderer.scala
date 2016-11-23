package bhuj.rendering

import bhuj._
import bhuj.model._

private[bhuj] class Renderer(scalaConverter: ScalaConverter, compiler: TemplateCompiler) {

  def rendered(template: Template, context: Context)(implicit global: Context = emptyContext): Result = {
    // FIXME the global needs passing all the way down still, and tests need to be written for this
    val tools = new Tools

    val res = for {
      scala <- scalaConverter.scala(template).right
      render <- compiler.compiled(scala).right
      result <- render(tools)(context).right
    } yield { result }

    res.left.map(_ => RenderFailure) // FIXME propagate failure
  }
//
//  private def rendered(component: Component, context: Context): Result = component match {
//    case section: Section => renderedSection(section, context)
//    case section: InvertedSection => renderedInvertedSection(section, context)
//    case Partial(name) => {
//      for {
//        template <- templates(name).toRight({ TemplateNotFound(name) }).right
//        parsed <- parse(template).right
//        result <- rendered(parsed, context).right
//      } yield { result }
//    }
//  }
//
//  private def renderedInvertedSection(section: InvertedSection, context: Context) = {
//    context.get(section.name).map {
//      case false => rendered(section.template, context)(emptyContext)
//      case None => rendered(section.template, context)(emptyContext)
//      case iterable: Iterable[Context]@unchecked if iterable.isEmpty => rendered(section.template, context)(emptyContext)
//      case _ => emptyResult
//    }.getOrElse(rendered(section.template, context))
//  }
//
//  private def renderedSection(section: Section, context: Context) = {
//    context.get(section.name).map {
//      case true => rendered(section.template, context)(emptyContext)
//      case lambda: Lambda @unchecked =>
//        val render: NonContextualRender = (templateString) => for {
//          template <- parse(templateString).right
//          result <- rendered(template, context).right
//        } yield { result }
//        lambda(formatter.source(section.template), render).left.map{ f: Any => LambdaFailure(section.name, f) }
//      case map: Context @unchecked => rendered(section.template, map)
//      case iterable: Iterable[Context] @unchecked => iterable.foldLeft(emptyResult) {
//        case (Right(acc), ctx) => rendered(section.template, ctx)(emptyContext).right.map(acc + _)
//        case (Left(fail), _) => Left(fail)
//      }
//      case Some(item) => item match {
//        case ctx: Context @unchecked => rendered(section.template, ctx)(emptyContext)
//        case nonCtx => rendered(section.template, Map("_" -> nonCtx))(emptyContext)
//      }
//      case _ => emptyResult
//    }.getOrElse(emptyResult)
//  }
//
//  private lazy val formatter = new Formatter

}
