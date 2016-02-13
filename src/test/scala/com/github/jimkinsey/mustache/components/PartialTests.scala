package com.github.jimkinsey.mustache.components

import com.github.jimkinsey.mustache._
import org.mockito.Mockito.when
import org.scalatest.FunSpec
import org.scalatest.Matchers._
import org.scalatest.mock.MockitoSugar.mock

class PartialTests extends FunSpec {
  implicit val global: Context = Map.empty

  describe("A partial component") {

    it("propagates failure to render the template") {
      val rendered: (String, Context) => Either[Any, String] = (_,_) => Left("BOOM!")
      new Partial("partial", rendered).rendered(Map.empty) should be(Left("BOOM!"))
    }

    it("renders the named template in the provided context") {
      val rendered: (String, Context) => Either[Any, String] = (_,_) => Right("42")
      new Partial("partial", rendered).rendered(Map("foo" -> 42)) should be(Right("42"))
    }

  }

}