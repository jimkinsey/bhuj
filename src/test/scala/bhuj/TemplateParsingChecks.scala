package bhuj

import org.scalacheck.Shrink
import org.scalatest.Matchers._
import org.scalatest.PropSpec
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class TemplateParsingChecks extends PropSpec with GeneratorDrivenPropertyChecks with TemplateGenerators {

  implicit val noShrink = Shrink[String] { _ => Stream.empty }

  property("valid template does not result in parse error") {
    forAll(template() -> "template") { template =>
      mustache.parse(template) should not be a[ParseFailure]
    }
  }

  private lazy val mustache = new Mustache()
}
