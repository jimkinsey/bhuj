import bhuj.parsing.Delimiters

package object bhuj {
  type Context = Map[String, Any]
  type Result = Either[Failure, String]
  type Lambda = (String, NonContextualRender) => Either[Any, String]
  type Render = (String, Context) => Result
  type ParseFailure = Left[Parse, String]

  private[bhuj] val emptyResult: Result = Right("")

  private[bhuj] val doubleMustaches = Delimiters("{{", "}}")

  private type NonContextualRender = (String) => Result
}


