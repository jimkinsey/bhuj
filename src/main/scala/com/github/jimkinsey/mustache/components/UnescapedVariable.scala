package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.rendering.Component
import com.github.jimkinsey.mustache.rendering.Renderer._

case class UnescapedVariable(name: String) extends Component {
  def rendered(context: Context) = Right(context.get(name).map(_.toString).getOrElse(""))
}
