package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.Context

class Partial(name: String, template: => Template) extends Value {
  override def rendered(context: Context)(implicit global: Context): Either[Any, String] = {
    template.rendered(context)
  }
}
