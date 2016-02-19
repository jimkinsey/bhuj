package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.context.CanContextualise

sealed trait Failure
sealed trait Parse { self: Failure => }
case class UnclosedTag(key: String) extends Failure with Parse
case class InvalidDelimiters(start: String, end: String) extends Failure with Parse
case class ContextualisationFailure(failure: CanContextualise.Failure) extends Failure
case class TemplateNotFound(name: String) extends Failure