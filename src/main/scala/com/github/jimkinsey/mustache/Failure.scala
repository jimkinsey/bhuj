package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.context.CanContextualise

sealed trait Failure
case class UnclosedTag(key: String) extends Failure
case class InvalidDelimiters(start: String, end: String) extends Failure
case class ContextualisationFailure(failure: CanContextualise.Failure) extends Failure
case class TemplateNotFound(name: String) extends Failure