package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.rendering.Component
import com.github.jimkinsey.mustache.rendering.Renderer.Context

case class Text(content: String) extends Component {
  def rendered(context: Context): Either[Component.Failure, String] = Right(content)
}
