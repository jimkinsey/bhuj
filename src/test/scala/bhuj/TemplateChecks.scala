package bhuj

import org.scalacheck.Shrink
import org.scalatest.Matchers._
import org.scalatest.PropSpec
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class TemplateChecks extends PropSpec with GeneratorDrivenPropertyChecks with TemplateGenerators {

  import bhuj.context.ContextImplicits._

  implicit val noShrink = Shrink[String] { _ => Stream.empty }

  property("valid template does not result in parse error") {
    forAll(template() -> "template") { template =>
      mustache.render(template, context) should not be a[ParseFailure]
    }
  }

  private lazy val mustache = new Mustache()

  private lazy val context: Map[String,Any] = Map(
    "boolean" -> true,
    "number" -> 42,
    "string" -> "Charlotte",
    "iterable" -> Seq(Map("id" -> 4), Map("id" -> 5))
  )
}
