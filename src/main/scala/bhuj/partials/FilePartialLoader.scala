package bhuj.partials

import java.io.FileNotFoundException

import scala.io.Source
import scala.util.Try

class FilePartialLoader(path: String) {

  def partial(name: String): Option[String] = {
    Try(Source.fromFile(s"$path/$name.mustache").mkString).recover {
      case _: FileNotFoundException =>
        Source.fromInputStream(getClass.getResourceAsStream(s"$path/$name.mustache")).mkString
    }.toOption
  }

}
