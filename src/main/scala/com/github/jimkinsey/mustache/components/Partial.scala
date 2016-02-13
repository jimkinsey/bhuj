package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.Context

class Partial(val name: String, val render: (String, Context) => Either[Any, String]) extends Value {
  override def rendered(context: Context)(implicit global: Context): Either[Any, String] = {
    render(name, context)
  }
}
