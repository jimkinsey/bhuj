package bhuj.rendering

import bhuj.Mustache.{Templates, emptyTemplates}
import bhuj.{Failure, ParseTemplate, TemplateNotFound}
import bhuj.model._

private[bhuj] class Optimiser(parse: ParseTemplate, templates: Templates = emptyTemplates) {

  def optimise(template: Template): Either[Failure, Template] = {
    template.components.foldLeft[Either[Failure, Template]](Right(template.copy(components = Seq.empty))) {
      case (Right(acc), partial: Partial) => optimisedPartial(partial).map(appendTo(acc))
      case (Right(acc), section: Section) => optimisedSection(section).map(section => appendTo(acc)(section))
      case (Right(acc), component)        => Right(appendTo(acc)(component))
      case (failure: Left[_,_], _)        => failure
    }
  }

  private def optimisedPartial(partial: Partial): Either[Failure, Seq[Component]] = for {
    template    <- partialTemplate(partial)
    isRecursive <- hasDescendant(template, partial)
    optimised   <- if (!isRecursive) optimise(template) else Right(template)
  } yield {
    if (isRecursive)
      Seq(partial)
    else
      optimised.components
  }

  private def partialTemplate(partial: Partial): Either[Failure, Template] = for {
    rawTemplate <- templates(partial.name).toRight({ TemplateNotFound(partial.name) })
    template    <- parse(rawTemplate)
  } yield { template }

  private def hasDescendant(template: Template, component: Component): Either[Failure, Boolean] = {
    template.components.foldLeft[Either[Failure, Boolean]](Right(false)) {
      case (Right(true), _)                    => Right(true)
      case (Right(false), c) if c == component => Right(true)
      case (Right(false), c: Container)        => hasDescendant(c.template, component)
      case (Right(false), c: Partial)          => partialTemplate(c).flatMap(hasDescendant(_, component))
      case (Right(false), _)                   => Right(false)
      case (failure: Left[_,_], _)             => failure
    }
  }

  private def optimisedSection(section: Section): Either[Failure, Section] = optimise(section.template).map(ot => section.copy(template = ot))

  private def appendTo(template: Template)(components: Component*) = template.copy(components = template.components ++ components)

}
