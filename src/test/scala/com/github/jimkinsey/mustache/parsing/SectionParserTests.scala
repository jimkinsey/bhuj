package com.github.jimkinsey.mustache.parsing

import com.github.jimkinsey.mustache.rendering.Template
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class SectionParserTests extends FunSpec {

  describe("A section parser") {

    it("matches a name beginning with a hash character") {
      SectionParser.pattern.findPrefixOf("#identifier") should be(defined)
    }

    it("does not match a name that does not begin with a hash character") {
      SectionParser.pattern.findPrefixOf("identifier") should not be defined
    }

    it("returns a section component with the name") {
      val template = Template()
      SectionParser.parsed("section", template).right.get.name should be("section")
    }

    it("returns a section component with the template") {
      val template = Template()
      SectionParser.parsed("section", template).right.get.template should be(template)
    }

  }

}
