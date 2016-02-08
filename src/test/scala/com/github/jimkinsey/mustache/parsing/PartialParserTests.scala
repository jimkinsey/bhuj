package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.Mustache.TemplateNotFound
import com.github.jimkinsey.mustache.TemplateParser
import org.scalatest.FunSpec
import org.scalatest.Matchers._
import org.scalatest.mock.MockitoSugar.mock
import org.mockito.Mockito.when

class PartialParserTests extends FunSpec {

  describe("A partial parser") {

    it("fails if the partial name is unknown") {
      new PartialParser(_ => None, null).parsed("bar") should be(Left(TemplateNotFound("bar")))
    }

    it("propagates failure to parse the template") {
      val failure = mock[TemplateParser.Failure]
      val templateParser = mock[TemplateParser]
      when(templateParser.parse("template")).thenReturn(Left(failure))
      new PartialParser(_ => Some("template"), templateParser).parsed("foo") should be(Left(failure))
    }
  }

}
