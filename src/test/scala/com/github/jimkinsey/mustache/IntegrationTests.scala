package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.context.ContextImplicits
import com.github.jimkinsey.mustache.tags.SectionStart.Lambda
import org.scalatest.FunSpec
import org.scalatest.Matchers._

import scala.language.postfixOps

class IntegrationTests extends FunSpec with TemplateFiles {
  import ContextImplicits._
  import MustacheBuilder._

  describe("Mustache rendering") {

    it("allows for rapid turnaround by not caching template files") {
      val mustache = mustacheRenderer.withTemplatePath(templateDirPath).withoutCache
      require(templateFile("greeting", "hello {{name}}").exists())
      mustache.renderTemplate("greeting", Person("Jim")) should be(Right("hello Jim"))
      require(templateFile("greeting", "HELLO {{name}}").exists())
      mustache.renderTemplate("greeting", Person("Jim")) should be(Right("HELLO Jim"))
    }

    it("allows for better performance by caching template files") {
      val mustache = mustacheRenderer.withTemplatePath(templateDirPath).withCache
      require(templateFile("greeting", "hello {{name}}").exists())
      mustache.renderTemplate("greeting", Person("Jim")) should be(Right("hello Jim"))
      require(templateFile("greeting", "HELLO {{name}}").exists())
      mustache.renderTemplate("greeting", Person("Jim")) should be(Right("hello Jim"))
    }

    it("provides a global context which is useful for localisation") {
      val localised: Lambda = (template, render) => render(template).right.map(_.replaceAll("Hello", "Bonjour"))
      val mustache = mustacheRenderer
        .withTemplates("greeting" -> "{{#localised}}Hello{{/localised}} {{name}}")
        .withHelpers("localised" -> localised)
      mustache.renderTemplate("greeting", Person("Elisabeth")) should be(Right("Bonjour Elisabeth"))
    }

  }

  val templateDirName = "integration-tests"

  private case class Person(name: String)

}

