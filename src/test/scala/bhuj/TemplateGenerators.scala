package bhuj

import org.scalacheck.Gen
import org.scalacheck.Gen._

import scala.util.Random

trait TemplateGenerators {
  def section(depth: Int = 0) = containerTag('#', depth)

  def invertedSection(depth: Int = 0) = containerTag('^', depth)

  private def containerTag(prefix: Char, depth: Int): Gen[String] = {
    if (depth >= MAX_DEPTH) return const("")
    for {
      inner <- template(depth + 1)
      key <- oneOf(sectionKeys.toSeq)
    } yield s"\n${pad(depth)}{{$prefix$key}}\n${pad(depth + 1)}$inner\n${pad(depth)}{{/$key}}\n"
  }

  lazy val variable = oneOf(variableKeys.toSeq).map(key => s"{{$key}}")

  lazy val tripleDelimitedVariable = oneOf(variableKeys.toSeq).map(key => s"{{{$key}}}")

  lazy val ampersandPrefixedVariable = oneOf(variableKeys.toSeq).map(key => s"{{&$key}}")

  lazy val text =
    (1 to Random.nextInt(MAX_WORDS_IN_TEXT)).foldLeft(const("")) {
      case (acc, _) => for {
        head <- aWord
        tail <- acc
      } yield head + " " + tail
    }

  lazy val aWord = for {
    len <- chooseNum(1, MAX_WORD_LENGTH)
    str <- alphaStr
  } yield str.take(len)

  lazy val comment = text.map(t => s"{{!$t}}")

  def template(depth: Int = 0): Gen[String] = {
    (1 to MAX_ROOT_NODES).foldLeft(const("")) {
      case (acc, _) => for {
        tail <- acc
        head <- oneOf(
          section(depth),
          invertedSection(depth),
          variable,
          tripleDelimitedVariable,
          ampersandPrefixedVariable,
          text,
          comment)
      } yield head + tail
    }
  }

  private def pad(depth: Int) = (1 to depth).map(_ => "  ").mkString

  private lazy val sectionKeys = Set("boolean", "iterable")
  private lazy val variableKeys = Set("number", "string")

  private val MAX_DEPTH = 3
  private val MAX_ROOT_NODES = 10
  private val MAX_WORD_LENGTH = 13
  private val MAX_WORDS_IN_TEXT = 20

}
