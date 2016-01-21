package com.github.jimkinsey.mustache

import java.io.{File, PrintWriter}

import com.github.jimkinsey.mustache.FilePartialLoader.Cache
import org.scalatest.FunSpec
import org.scalatest.Matchers._

import scala.collection.mutable

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

    it("uses the cache to store the partial if it is specified") {
      require(templateFile("result", "{{a}} + {{b}} = {{c}}").exists())
      val cache = mutable.Map[String,String]()
      loader(cache).partial("result")
      cache should contain("result" -> "{{a}} + {{b}} = {{c}}")
    }

    it("retrieves the partial from the cache when present") {
      val cache = mutable.Map("cached" -> "Template")
      loader(cache).partial("cached") should be(Some("Template"))
    }

  }

  private val testPath = getClass.getResource("/templates").getPath

  private def loader: FilePartialLoader = new FilePartialLoader(testPath)

  private def loader(cache: Cache): FilePartialLoader = new FilePartialLoader(testPath, Some(cache))

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
