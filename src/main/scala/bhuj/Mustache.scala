package bhuj

import bhuj.Mustache._
import bhuj.context.{CanContextualise, CanContextualiseMap, CaseClassConverter}
import bhuj.parsing._
import bhuj.partials.Caching
import bhuj.rendering.Renderer
import scala.concurrent.{ExecutionContext, Future}

object Mustache {
  type Templates = (String => Future[Option[String]])
  lazy val emptyTemplates: Templates = _ => Future successful None
}

class Mustache(
  templates: Templates = emptyTemplates,
  implicit val globalContext: Context = Map.empty) {

  import bhuj.result.EventualResult._

  def renderTemplate[C](name: String, context: C)(implicit ev: CanContextualise[C], ec: ExecutionContext): Future[Result] = {
    for {
      template  <- templates(name)                                        |> fromFutureOption({TemplateNotFound(name)})
      parsed    <- parse(template)                                        |> fromEither
      ctx       <- ev.context(context).left.map(ContextualisationFailure) |> fromEither
      result    <- renderer.rendered(parsed, ctx)                      |> fromFutureEither
    } yield { result }
  }

  def render[C](template: String, context: C)(implicit ev: CanContextualise[C], ec: ExecutionContext): Future[Result] = {
    for {
      parsed    <- parse(template)                                        |> fromEither
      ctx       <- ev.context(context).left.map(ContextualisationFailure) |> fromEither
      rendered  <- renderer.rendered(parsed, ctx)                      |> fromFutureEither
    } yield { rendered }
  }

  def render(template: String)(implicit ec: ExecutionContext): Future[Result]= {
    for {
      parsed    <- parse(template)                            |> fromEither
      rendered  <- renderer.rendered(parsed, emptyContext) |> fromFutureEither
    } yield { rendered }
  }

  private lazy val renderer = new Renderer(parse, templates)

  private implicit val canContextualiseMap: CanContextualiseMap = new CanContextualiseMap(new CaseClassConverter)

  private lazy val templateParser: TemplateParser = new TemplateParser(
    TextParser,
    VariableParser,
    TripleDelimitedVariableParser,
    AmpersandPrefixedVariableParser,
    CommentParser,
    SectionParser,
    InvertedSectionParser,
    PartialParser,
    SetDelimitersParser)

  private implicit val parserConfig: ParserConfig = ParserConfig(parse, doubleMustaches)

  private[bhuj] lazy val parse: ParseTemplate = Caching.cached(templateParser.template)

}