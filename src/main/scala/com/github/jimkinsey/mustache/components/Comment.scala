package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache.Context

case class Comment(content: String) extends Value {
  override def rendered(context: Context)(implicit global: Context): Either[Any, String] = Right("")
}