package bhuj

import bhuj.Mustache._
import bhuj.context.{CanContextualise, CanContextualiseMap, CaseClassConverter}
import bhuj.parsing._
import bhuj.partials.Caching
import bhuj.rendering.{Optimiser, Renderer}

object Mustache {
  type Templates = (String => Option[String])
  lazy val emptyTemplates: Templates = Map.empty.get
}

class Mustache(
  templates: Templates = emptyTemplates,
  implicit val globalContext: Context = Map.empty) {

  def this(map: Map[String,String]) = {
    this(map.get _)
  }

  def renderTemplate[C](name: String, context: C)(implicit ev: CanContextualise[C]): Result = {
    for {
      template  <- templates(name).toRight({TemplateNotFound(name)}).right
      parsed    <- parse(template).right
      optimised <- optimise(parsed).right
      ctx       <- ev.context(context).left.map(ContextualisationFailure).right
      result    <- renderer.rendered(optimised, ctx).right
    } yield { result }
  }

  def render[C](template: String, context: C)(implicit ev: CanContextualise[C]): Result = {
    for {
      parsed    <- parse(template).right
      optimised <- optimise(parsed).right
      ctx       <- ev.context(context).left.map(ContextualisationFailure).right
      rendered  <- renderer.rendered(optimised, ctx).right
    } yield { rendered }
  }

  def render(template: String): Result = {
    for {
      parsed    <- parse(template).right
      optimised <- optimise(parsed).right
      rendered  <- renderer.rendered(optimised, emptyContext).right
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

  private lazy val optimiser: Optimiser = new Optimiser(parse, templates)

  private lazy val optimise: Optimise = Caching.cached(optimiser.optimise)

}