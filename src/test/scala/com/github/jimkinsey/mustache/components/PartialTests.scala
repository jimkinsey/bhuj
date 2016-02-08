package com.github.jimkinsey.mustache.components

import org.mockito.Mockito.when
import org.scalatest.FunSpec
import org.scalatest.Matchers._
import org.scalatest.mock.MockitoSugar.mock

class PartialTests extends FunSpec {

  describe("A partial component") {

    it("propagates failure to render the template") {
      val failing = mock[Template]
      when(failing.rendered(Map.empty)).thenReturn(Left("BOOM!"))
      new Partial("partial", failing).rendered(Map.empty) should be(Left("BOOM!"))
    }

    it("renders the template in the provided context") {
      val template: Template = Template(Variable("foo"))
      new Partial("partial", template).rendered(Map("foo" -> 42)) should be(Right("42"))
    }
  }

}
