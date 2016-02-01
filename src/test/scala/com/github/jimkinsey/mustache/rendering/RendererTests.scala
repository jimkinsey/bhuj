package com.github.jimkinsey.mustache.rendering

import com.github.jimkinsey.mustache.rendering.Renderer._
import com.github.jimkinsey.mustache.tags.{Partial, Variable}
import org.scalatest.FunSpec
import org.scalatest.Matchers._

import scala.util.matching.Regex

class RendererTests extends FunSpec {

  describe("A renderer") {

    it("fails if one of the components of the template fails") {
      val renderer = new Renderer()
      val failure = new Component.Failure {}
      val failingComponent = new Component {
        def rendered = Left(failure)
      }
      renderer.render(Template(failingComponent)) should be(Left(RenderFailure(failure)))
    }

    it("returns an empty result if the template has no components") {
      new Renderer().render(Template(Seq.empty:_*)) should be(Right(""))
    }

    it("concatenates the results of rendering each component") {
      def foo(n: Int) = new Component {
        val rendered = Right(s"foo$n")
      }
      new Renderer().render(Template(foo(1), foo(2), foo(3))) should be(Right("foo1foo2foo3"))
    }

  }

}
