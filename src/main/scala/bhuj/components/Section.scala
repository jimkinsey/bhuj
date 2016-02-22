package bhuj.components

import bhuj.components.Partial.RenderTemplate
import bhuj.emptyResult
import bhuj.parsing.Delimiters
import bhuj._

private[bhuj] case class Section(name: String, template: Template, private val rendered: Render) extends Container {
  def rendered(context: Context)(implicit global: Context) = {
    context.get(name).map {
      case true => template.rendered(context)
      case lambda: Lambda @unchecked =>
        lambda(template.source, rendered(_, context))
      case map: Context @unchecked => template.rendered(map)
      case iterable: Iterable[Context] @unchecked => iterable.foldLeft(emptyResult) {
        case (Right(acc), ctx) => template.rendered(ctx).right.map(acc + _)
        case (Left(fail), _) => Left(fail)
      }
      case Some(ctx: Context @unchecked) => template.rendered(ctx)
      case _ => emptyResult
    }.getOrElse(emptyResult)
  }

  def formatted(delimiters: Delimiters) = s"${delimiters.tag(s"#$name")}${template.formatted(delimiters)}${delimiters.tag(s"/$name")}"

}

private[bhuj] case class InvertedSection(name: String, template: Template, render: RenderTemplate) extends Container {
  def rendered(context: Context)(implicit global: Context) = {
    context.get(name).map {
      case false => template.rendered(context)
      case None => template.rendered(context)
      case iterable: Iterable[Context]@unchecked if iterable.isEmpty => template.rendered(context)
      case _ => emptyResult
    }.getOrElse(template.rendered(context))
  }

  def formatted(delimiters: Delimiters) = s"${delimiters.tag(s"^$name")}${template.formatted(delimiters)}${delimiters.tag(s"/$name")}"

}
