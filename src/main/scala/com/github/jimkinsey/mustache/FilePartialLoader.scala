package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.FilePartialLoader.Cache

import scala.io.Source
import scala.util.Try

import scala.collection.mutable

object FilePartialLoader {
  type Cache = mutable.Map[String,String]
}

class FilePartialLoader(path: String, cache: Option[Cache] = None) {

  def partial(name: String): Option[String] = {
    cache
      .flatMap(_.get(name))
      .orElse(
        Try(Source.fromFile(s"$path/$name.mustache").mkString)
          .toOption
          .map { template =>
            cache.map(_.put(name, template))
            template
          }
      )
  }

}
