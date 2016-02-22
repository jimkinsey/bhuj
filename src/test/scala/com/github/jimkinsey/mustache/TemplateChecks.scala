package com.github.jimkinsey.mustache

import org.scalacheck.Gen.{alphaStr, chooseNum, const, oneOf}
import org.scalacheck.{Gen, Shrink}
import org.scalatest.Matchers._
import org.scalatest.PropSpec
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import scala.util.Random

class TemplateChecks extends PropSpec with GeneratorDrivenPropertyChecks with TemplateGenerators {

  import com.github.jimkinsey.mustache.context.ContextImplicits._

  implicit val noShrink = Shrink[String] { _ => Stream.empty }

  property("valid template does not result in parse error") {
    forAll(template() -> "template") { template =>
      mustache.render(template, context) should not be a[ParseFailure]
    }
  }

  private lazy val mustache = new Mustache()

  private lazy val context: Map[String,Any] = Map(
    "boolean" -> true,
    "number" -> 42,
    "string" -> "Charlotte",
    "iterable" -> Seq(Map("id" -> 4), Map("id" -> 5))
  )
}

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
