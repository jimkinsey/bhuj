package com.github.jimkinsey

import com.github.jimkinsey.mustache.components.Section

package object mustache {
  type Context = Map[String, Any]
  type Result = Either[Any, String]
  type Lambda = Section.Lambda
}
