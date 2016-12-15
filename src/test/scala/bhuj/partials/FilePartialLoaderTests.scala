package bhuj.partials

import bhuj.TemplateFiles
import org.scalatest.Matchers._
import org.scalatest.{AsyncFunSpec, BeforeAndAfterEach}

class FilePartialLoaderTests extends AsyncFunSpec with BeforeAndAfterEach with TemplateFiles {

  describe("A file partial loader") {

    it("returns no template if the file does not exist") {
      require(!templateFile("does-not-exist").exists())
      loader.partial("does-not-exist") map (_ should not be defined)
    }

    it("returns the content of the file if it exists") {
      require(templateFile("greeting", "Hello {{name}}!").exists())
      loader.partial("greeting") map (_ should be(Some("Hello {{name}}!")))
    }

  }

  val templateDirName = "file-partial-loader-tests"

  private def loader: FilePartialLoader = new FilePartialLoader(templateDirPath)

}
