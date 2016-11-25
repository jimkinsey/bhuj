package bhuj.rendering

import bhuj.model._

private[bhuj] class ScalaConverter {

  def scala(template: Template): Either[bhuj.Failure, String] = {
    val code = template.components.foldLeft(""""""""){
      case (acc, Text(text))                     => acc + s""" + \"\"\"${escaped(text)}\"\"\""""
      case (acc, variable: UnescapedVariable)    => acc + s""" + context.getOrElse("${variable.name}", "")"""
      case (acc, Variable(name))                 => acc + s""" + context.get("$name").map(value => tools.escapeHTML(value.toString)).getOrElse("")"""
      case (acc, Partial(name))                  => acc + s""" + tools.renderedPartial("$name")"""
      case (acc, section: Section)               => acc + s""" + tools.renderedSection(${section.hashCode})"""
      case (acc, section: InvertedSection)       => acc + s""" + tools.renderedSection(${section.hashCode})"""
      case (acc, _) => acc
    }
    Right(s"""(tools: bhuj.rendering.Tools) => (context: bhuj.Context) => Right($code)""")
  }

  private def escaped(text: String): String = text.replaceAll("""\"\"\"""", """\\\"\\\"\\\"""")

}

private[bhuj] object ScalaConverter {

  sealed trait Failure

}