package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.components.{Comment, Variable, UnescapedVariable, Component}

trait StandaloneTagComponentParser[+T <: Component] extends ComponentParser[T] {
  def contentPattern: String
  def constructor: (String => T)

  final def parseResult(template: String)(implicit parserConfig: ParserConfig): Either[Any, Option[ParseResult[T]]] = {
    Right(s"""\\{\\{$contentPattern\\}\\}""".r.findPrefixMatchOf(template).map { tagMatch =>
      ParseResult[T](constructor(tagMatch.group(1)), tagMatch.after.toString)
    })         // also need failure conditions for pattern being invalid
  }
}

object UnescapedVariableParser extends StandaloneTagComponentParser[UnescapedVariable] {
  lazy val contentPattern = "\\{([a-zA-Z0-9]+?)\\}"
  lazy val constructor = UnescapedVariable.apply _
}

object VariableParser extends StandaloneTagComponentParser[Variable] {
  lazy val contentPattern = "([a-zA-Z0-9]+?)"
  lazy val constructor = Variable.apply _
}

object CommentParser extends StandaloneTagComponentParser[Comment] {
  lazy val contentPattern = "(?s)!(.*?)"
  lazy val constructor = Comment.apply _
}
