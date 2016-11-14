package bhuj.rendering

import bhuj.components.Component
import bhuj.{Context, Result}

private[bhuj] class Renderer {

  def rendered(component: Component, context: Context)(implicit global: Context): Result = component.rendered(context)

}
