package com.github.jimkinsey.mustache.partials

import java.io.{File, PrintWriter}

import com.github.jimkinsey.mustache.TemplateFiles
import org.scalatest.{Suite, BeforeAndAfterEach, FunSpec}
import org.scalatest.Matchers._

class FilePartialLoaderTests extends FunSpec with BeforeAndAfterEach with TemplateFiles {

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

  val templateDirName = "file-partial-loader-tests"

  private def loader: FilePartialLoader = new FilePartialLoader(templateDirPath)

}
