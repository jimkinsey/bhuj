package com.github.jimkinsey.mustache

object CaseClassConverter {
  sealed trait Failure
  case class GeneralFailure(message: String) extends Failure
}

trait CaseClassConverter {
  def map(caseClass: Product): Either[CaseClassConverter.Failure, Map[String, Any]]
}

