package com.github.jimkinsey.mustache

import scala.collection.mutable

object Caching {
  def cached[K, V](fn: K => V): K => V = {
    val cache = mutable.Map[K, V]()
    (key: K) => cache.getOrElse(key, {
      val value = fn(key)
      cache.put(key, value)
      value
    })
  }

  implicit class AddCaching[K,V](fn: K => V) {
    lazy val withCache: K => V = cached(fn)
  }
}
