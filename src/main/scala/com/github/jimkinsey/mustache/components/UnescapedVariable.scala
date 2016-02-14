package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.Context

private[mustache] case class UnescapedVariable(name: String) extends Value {
  def rendered(context: Context)(implicit global: Context) = {
    Right((global ++ context).get(name).map(_.toString).getOrElse(""))
  }
}
