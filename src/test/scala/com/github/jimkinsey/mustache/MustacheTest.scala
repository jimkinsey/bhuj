package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.Mustache.TemplateNotFound
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class MustacheTest extends FunSpec {

  describe("A Mustache renderer") {

    it("returns a TemplateNotFound failure when asked to render a template not known to it") {
      new Mustache(templates = Map.empty).renderTemplate("page") should be(Left(TemplateNotFound("page")))
    }

    it("returns the result of rendering the named template when it is available") {
      new Mustache(templates = Map("page" -> "A page!")).renderTemplate("page") should be(Right("A page!"))
    }

  }

}
