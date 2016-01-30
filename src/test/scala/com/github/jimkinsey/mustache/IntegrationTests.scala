package com.github.jimkinsey.mustache

import java.io.{File, PrintWriter}

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class IntegrationTests extends FunSpec {
  import ContextImplicits._

  describe("Mustache rendering") {
    val templateLoader = new FilePartialLoader(getClass.getClassLoader.getResource("templates").getPath)

    it("allows for rapid turnaround by not caching template files") {
      val mustache = new Mustache(templates = templateLoader.partial)
      writeTemplate("greeting", "hello {{name}}")
      val model: Map[String, Any] = Map("name" -> "Jim")
      mustache.renderTemplate("greeting", model) should be(Right("hello Jim"))
      writeTemplate("greeting", "Hello {{name}}")
      mustache.renderTemplate("greeting", model) should be(Right("Hello Jim"))
    }

    it("allows for better performance by caching template files") {
      val mustache = new Mustache(templates = Caching.cached(templateLoader.partial))
      writeTemplate("greeting", "hello {{name}}")
      val model: Map[String, Any] = Map("name" -> "Jim")
      mustache.renderTemplate("greeting", model) should be(Right("hello Jim"))
      writeTemplate("greeting", "Hello {{name}}")
      mustache.renderTemplate("greeting", model) should be(Right("hello Jim"))
    }

  }

  private def writeTemplate(name: String, content: String) = {
    val dir = getClass.getClassLoader.getResource("templates").getPath
    val file = new File(s"$dir/$name.mustache")
    val writer = new PrintWriter(file)
    writer.write(content)
    writer.close()
  }

}

