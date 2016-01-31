package com.github.jimkinsey.mustache

import java.io.{File, PrintWriter}

import com.github.jimkinsey.mustache.context.ContextImplicits
import org.scalatest.FunSpec
import org.scalatest.Matchers._

import scala.language.postfixOps

class IntegrationTests extends FunSpec {
  import ContextImplicits._
  import MustacheBuilder._

  describe("Mustache rendering") {

    it("allows for rapid turnaround by not caching template files") {
      val mustache = mustacheRenderer.withTemplatePath(templatePath).withoutCache
      writeTemplate("greeting", "hello {{name}}")
      mustache.renderTemplate("greeting", Person("Jim")) should be(Right("hello Jim"))
      writeTemplate("greeting", "Hello {{name}}")
      mustache.renderTemplate("greeting", Person("Jim")) should be(Right("Hello Jim"))
    }

    it("allows for better performance by caching template files") {
      val mustache = mustacheRenderer.withTemplatePath(templatePath).withCache
      writeTemplate("greeting", "hello {{name}}")
      mustache.renderTemplate("greeting", Person("Jim")) should be(Right("hello Jim"))
      writeTemplate("greeting", "Hello {{name}}")
      mustache.renderTemplate("greeting", Person("Jim")) should be(Right("hello Jim"))
    }

  }

  private val templatePath: String = getClass.getClassLoader.getResource("templates").getPath

  private def writeTemplate(name: String, content: String) = {
    val file = new File(s"$templatePath/$name.mustache")
    val writer = new PrintWriter(file)
    writer.write(content)
    writer.close()
  }

  private case class Person(name: String)

}

