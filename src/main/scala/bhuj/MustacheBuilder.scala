package bhuj

import bhuj.partials.FilePartialLoader

import scala.concurrent.Future
import scala.language.implicitConversions

object MustacheBuilder {
  val mustacheRenderer = MustacheBuilder()

  implicit def mustacheRendererBuilt(builder: MustacheBuilder): Mustache = builder.build
}

case class MustacheBuilder(
  templatePath: Option[String] = None,
  templates: Option[Map[String,String]] = None,
  cached: Boolean = false,
  globalContext: Context = Map[String,Any]()) {

  import bhuj.partials.Caching._

  def withTemplatePath(path: String) = copy(templatePath = Some(path))
  def withTemplates(templates: (String, String)*) = copy(templates = Some(templates.toMap))
  def withCache = copy(cached = true)
  def withoutCache = copy(cached = false)
  def withCacheEnabled(enabled: Boolean) = copy(cached = enabled)
  def withHelpers(helpers: (String, Lambda)*) = copy(globalContext = globalContext ++ helpers.toMap)
  def withGlobalValues(pairs: (String, Any)*) = copy(globalContext = globalContext ++ pairs.toMap)

  lazy val build = {
    val partials: Mustache.Templates =
      templatePath
        .map(path => new FilePartialLoader(path).partial _)
        .orElse(templates.map( map => (name: String) => Future.successful(map.get(name)) ))
        .map(fn => if (cached) fn.withCache else fn)
        .getOrElse(Mustache.emptyTemplates)
    new Mustache(partials, globalContext)
  }
}
