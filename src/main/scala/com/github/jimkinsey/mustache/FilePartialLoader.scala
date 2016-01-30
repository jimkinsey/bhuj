package com.github.jimkinsey.mustache

import scala.io.Source
import scala.util.Try

class FilePartialLoader(path: String) {

  def partial(name: String): Option[String] = {
    Try(Source.fromFile(s"$path/$name.mustache").mkString).toOption
  }

}
