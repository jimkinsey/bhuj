import bhuj.model.Template
import bhuj.parsing.Delimiters

import scala.concurrent.Future

package object bhuj {
  type Context = Map[String, Any]
  type Result = Either[Failure, String]
  type Lambda = (String, NonContextualRender) => Future[Either[Any, String]]
  type ParseFailure = Left[Parse, String]

  private[bhuj] type ParseTemplateFailure = Failure with Parse

  private[bhuj] type ParseTemplate = (String => Either[ParseTemplateFailure, Template])

  private[bhuj] val emptyResult: Result = Right("")

  private[bhuj] val emptyContext: Context = Map.empty

  private[bhuj] val doubleMustaches = Delimiters("{{", "}}")

  private[bhuj] type NonContextualRender = (String) => Future[Result]
}


