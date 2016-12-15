package bhuj.rendering

import bhuj.Mustache.{Templates, emptyTemplates}
import bhuj.{Failure, ParseTemplate, TemplateNotFound}
import bhuj.model._
import bhuj.rendering.Optimiser.OptimiserResult
import bhuj.result.EventualResult

import scala.concurrent.{ExecutionContext, Future}

private[bhuj] object Optimiser {
  type OptimiserResult = Either[Failure,Template]
}

private[bhuj] class Optimiser(parse: ParseTemplate, templates: Templates = emptyTemplates) {
  import bhuj.result.EventualResult._

  def optimise(template: Template)(implicit ec: ExecutionContext): Future[OptimiserResult] = {
    lazy val zero: OptimiserResult = Right(template.copy(components = Seq.empty))

    val optimisingComponents = template.components.map {
      case partial: Partial => optimisedPartial(partial)
      case section: Section => optimisedSection(section).map(_.map(Seq.apply(_)))
      case component        => Future.successful(Right(Seq(component)))
    }.to[collection.immutable.Seq]

    Future.foldLeft(optimisingComponents)(zero) {
      case (acc: Right[_,_], Right(components)) => acc map append(components)
      case (_, Left(failure))                   => Left(failure)
      case (fail: Left[_,_], _)                 => fail
    }
  }

  private def optimisedPartial(partial: Partial)(implicit ec: ExecutionContext): Future[Either[Failure, Seq[Component]]] = {
    def possiblyOptimised(shouldOptimise: Boolean, template: Template): Future[Either[Failure, Template]] =
      if (!shouldOptimise) optimise(template) else Future.successful[OptimiserResult](Right(template))

    for {
      template    <- partialTemplate(partial)                 |> fromFutureEither[Template]
      isRecursive <- hasDescendant(template, partial)         |> fromFutureEither[Boolean]
      optimised   <- possiblyOptimised(isRecursive, template) |> fromFutureEither[Template]
    } yield {
      if (isRecursive)
        Seq(partial)
      else
        optimised.components
    }
  }

  private def partialTemplate(partial: Partial)(implicit ec: ExecutionContext): Future[OptimiserResult] = {
    val res: EventualResult[Template] = for {
      rawTemplate <- templates(partial.name) |> fromFutureOption[String]({ TemplateNotFound(partial.name) })
      template    <- parse(rawTemplate)      |> fromEither
    } yield { template }
    res
  }

  private def hasDescendant(template: Template, component: Component)(implicit ec: ExecutionContext): Future[Either[Failure, Boolean]] = {
    lazy val zero: Either[Failure,Boolean] = Right(false)

    val checkingComponents = template.components.map {
      case c if c == component => Future.successful(Right(true))
      case c: Container        => hasDescendant(c.template, component)
      case c: Partial          => ToResult(for {
          partial       <- partialTemplate(c) |> fromFutureEither
          hasDescendant <- hasDescendant(partial, component) |> fromFutureEither
        } yield { hasDescendant })
      case _                   => Future.successful(Right(false))
    }.to[collection.immutable.Seq]

    Future.foldLeft(checkingComponents)(zero) {
      case (failure: Left[_,_], _)    => failure
      case (success @ Right(true), _) => success
      case (_, found : Right[Failure,Boolean])   => found
      case (_, failed : Left[_,_])    => failed
    }
  }

  private def optimisedSection(section: Section)(implicit ec: ExecutionContext): Future[Either[Failure, Section]] = optimise(section.template).map(_ map (ot => section.copy(template = ot)))

  private def append(components: Seq[Component])(template: Template) = template.copy(components = template.components ++ components)

}
