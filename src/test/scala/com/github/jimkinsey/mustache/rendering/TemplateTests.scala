package com.github.jimkinsey.mustache.rendering

import com.github.jimkinsey.mustache.components.{Variable, Template, Component}
import org.mockito.Mockito.when
import org.scalatest.FunSpec
import org.scalatest.Matchers._
import org.scalatest.mock.MockitoSugar.mock
import com.github.jimkinsey.mustache.Context

class TemplateTests extends FunSpec {
  implicit val global: Context = Map.empty

  describe("A template") {

    it("renders to an empty string if it has no components") {
      Template().rendered(Map.empty) should be(Right(""))
    }

    it("propagates the failure of any components") {
      val failure = "BOOM"
      val failing = mock[Component]
      when(failing.rendered(Map.empty)).thenReturn(Left(failure))
      Template(failing).rendered(Map.empty) should be(Left(failure))
    }

    it("concatenates the results of rendering all its components") {
      val component1 = mock[Component]
      when(component1.rendered(Map.empty)).thenReturn(Right("X"))
      val component2 = mock[Component]
      when(component2.rendered(Map.empty)).thenReturn(Right("Y"))
      Template(component1, component2).rendered(Map.empty) should be(Right("XY"))
    }

    it("uses values in the global context") {
      implicit val global: Context = Map("x" -> 2, "y" -> 3)
      Template(Variable("x"), Variable("y")).rendered(Map("x" -> 1)) should be(Right("13"))
    }

  }

}
