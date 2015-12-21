package com.github.jimkinsey

import com.github.jimkinsey.Renderer.{Failure, Result, Context, Tag}

object Comment extends Tag {
  val pattern = """(?s)!(.+)""".r
  def process(name: String, context: Context, postTagTemplate: String, render: (String, Context) => Result): Either[Failure, (String, String)] =
    Right("", postTagTemplate)
}
