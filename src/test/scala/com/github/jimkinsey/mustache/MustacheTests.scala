package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.context.{ContextImplicits, CanContextualise}
import CanContextualise.ContextualisationFailure
import com.github.jimkinsey.mustache.Mustache.TemplateNotFound
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class MustacheTests extends FunSpec {
  import ContextImplicits.canContextualiseMap

  describe("A Mustache renderer") {
    pending

    it("returns a TemplateNotFound failure when asked to render a template not known to it") {
      new Mustache(templates = Map.empty.get).renderTemplate("page", Map[String,Any]()) should be(Left(TemplateNotFound("page")))
    }

    it("returns the result of rendering the named template when it is available") {
      new Mustache(templates = Map("page" -> "A page!").get).renderTemplate("page", Map[String,Any]()) should be(Right("A page!"))
    }

    it("returns the failure if the contextualiser cannot produce a context") {
      implicit object IntCanContextualise$ extends CanContextualise[Int] {
        def context(i: Int) = Left(ContextualisationFailure("Ints cannot be maps"))
      }
      val mustache = new Mustache(templates = Map("x" -> "x={{x}}").get)
      mustache.renderTemplate("x", 42) should be(Left(ContextualisationFailure("Ints cannot be maps")))
    }

    it("uses the available evidence to produce a context for rendering") {
      case class Person(name: String)
      implicit object PersonCanContextualise$ extends CanContextualise[Person] {
        def context(person: Person) = Right(Map("name" -> person.name))
      }
      val mustache = new Mustache(templates = Map("greeting" -> "Hello {{name}}!").get)
      mustache.renderTemplate("greeting", Person("Charlotte")) should be(Right("Hello Charlotte!"))
    }

    it("can be constructed with a global context to pass to the Renderer") {
      val mustache = new Mustache(globalContext = Map("theAnswer" -> 42))
      mustache.render("The answer is {{theAnswer}}") should be(Right("The answer is 42"))
    }

  }

}
