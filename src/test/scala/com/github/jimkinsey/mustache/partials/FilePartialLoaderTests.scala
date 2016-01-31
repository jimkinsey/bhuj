package com.github.jimkinsey.mustache.partials

import java.io.{File, PrintWriter}

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class FilePartialLoaderTests extends FunSpec {

  describe("A file partial loader") {

    it("returns no template if the file does not exist") {
      require(!templateFile("does-not-exist").exists())
      loader.partial("does-not-exist") should not be defined
    }

    it("returns the content of the file if it exists") {
      require(templateFile("greeting", "Hello {{name}}!").exists())
      loader.partial("greeting") should be(Some("Hello {{name}}!"))
    }

  }

  private val testPath = getClass.getResource("/templates").getPath

  private def loader: FilePartialLoader = new FilePartialLoader(testPath)

  private def templateFile(name: String, content: String): File = templateFile(name, Some(content))

  private def templateFile(name: String, content: Option[String] = None): File = {
    val file = new File(s"$testPath/$name.mustache")
    content.foreach { template =>
      val writer = new PrintWriter(file)
      writer.write(template)
      writer.close()
    }
    file
  }

}
