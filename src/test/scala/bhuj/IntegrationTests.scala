package bhuj

import bhuj.context.ContextImplicits
import org.scalatest.AsyncFunSpec
import org.scalatest.Matchers._

import scala.language.postfixOps

class IntegrationTests extends AsyncFunSpec with TemplateFiles {
  import ContextImplicits._
  import MustacheBuilder._

  describe("Mustache rendering") {

    it("allows for rapid turnaround by not caching template files") {
      val mustache = mustacheRenderer.withTemplatePath(templateDirPath).withoutCache
      require(templateFile("greeting", "hello {{name}}").exists())
      mustache.renderTemplate("greeting", Person("Jim")) flatMap { _ =>
        require(templateFile("greeting", "HELLO {{name}}").exists())
        mustache.renderTemplate("greeting", Person("Jim")) map (_ shouldBe Right("HELLO Jim"))
      }
    }

    it("allows for better performance by caching template files") {
      val mustache = mustacheRenderer.withTemplatePath(templateDirPath).withCache
      require(templateFile("greeting", "hello {{name}}").exists())
      mustache.renderTemplate("greeting", Person("Jim")) flatMap { _ =>
        require(templateFile("greeting", "HELLO {{name}}").exists())
        mustache.renderTemplate("greeting", Person("Jim")) map (_ shouldBe Right("hello Jim"))
      }
    }

    it("provides a global context which is useful for localisation") {
      val localised: Lambda = (template, render) => render(template).right.map(_.replaceAll("Hello", "Bonjour"))
      val mustache = mustacheRenderer
        .withTemplates("greeting" -> "{{#localised}}Hello{{/localised}} {{name}}")
        .withHelpers("localised" -> localised)
      mustache.renderTemplate("greeting", Person("Elisabeth")) map (_ shouldBe Right("Bonjour Elisabeth"))
    }

    it("honours the set delimiters tag when applying a lambda which renders the content") {
      val localised: Lambda = (template, render) => render(template).right.map(_.replaceAll("Hello", "Bonjour"))
      val mustache = mustacheRenderer
        .withTemplates("greeting" -> "{{#localised}}Hello {{=<% %>=}}<%name%> {{name}}{{/localised}}")
        .withHelpers("localised" -> localised)
      mustache.renderTemplate("greeting", Person("Charlotte")) map (_ shouldBe Right("Bonjour Charlotte {{name}}"))
    }

    it("switches the delimiters when the set delimiters tag is used") {
      mustacheRenderer.render("A: {{a}} {{=<% %>=}}B: <%b%> C: {{c}}", Map("a" -> 1, "b" -> 2, "c" -> 3)) map (_ shouldBe Right("A: 1 B: 2 C: {{c}}"))
    }

  }

  val templateDirName = "integration-tests"

  private case class Person(name: String)

}

