package bhuj.rendering

import bhuj._
import bhuj.rendering.TemplateCompiler.CompiledTemplate

private[bhuj] object TemplateCompiler {

  type CompiledTemplate = (Tools => Context => Result)

}

private[bhuj] class TemplateCompiler {
  import reflect.runtime.currentMirror
  import tools.reflect.ToolBox
  val toolbox = currentMirror.mkToolBox()

  def compiled(templateScala: String): Either[Failure, CompiledTemplate] = {
    Right(toolbox.compile(toolbox.parse(templateScala))().asInstanceOf[Tools => Context => Result])
  }

}