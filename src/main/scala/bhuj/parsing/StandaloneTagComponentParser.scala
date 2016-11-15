package bhuj.parsing

import bhuj.model._

private[bhuj] trait StandaloneTagComponentParser[+T <: Component] extends ComponentParser[T] {
  def contentPattern: String
  def constructor: (String => T)

  final def parseResult(template: String)(implicit parserConfig: ParserConfig) = {
    Right(parserConfig.delimiters.pattern(contentPattern).r.findPrefixMatchOf(template).map { tagMatch =>
      ParseResult[T](constructor(tagMatch.group(1)), tagMatch.after.toString)
    })
  }
}

private[bhuj] object TripleDelimitedVariableParser extends StandaloneTagComponentParser[TripleDelimitedVariable] {
  import VariableParser._
  lazy val contentPattern = s"\\{\\s*($variableKeyPattern?)\\s*\\}"
  lazy val constructor = TripleDelimitedVariable.apply _
}

private[bhuj] object AmpersandPrefixedVariableParser extends StandaloneTagComponentParser[AmpersandPrefixedVariable] {
  import VariableParser._
  lazy val contentPattern = s"&\\s*($variableKeyPattern?)\\s*"
  lazy val constructor = AmpersandPrefixedVariable.apply _
}

private[bhuj] object VariableParser extends StandaloneTagComponentParser[Variable] {
  lazy val contentPattern = s"\\s*($variableKeyPattern?)\\s*"
  lazy val constructor = Variable.apply _
  val variableKeyPattern = "[a-zA-Z0-9_]+"
}

private[bhuj] object CommentParser extends StandaloneTagComponentParser[Comment] {
  lazy val contentPattern = "(?s)!(.*?)"
  lazy val constructor = Comment.apply _
}
