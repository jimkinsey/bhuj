package bhuj.rendering

import bhuj.model._
import org.scalatest.FunSpec

class ScalaConverterTests extends FunSpec {
  import org.scalatest.Matchers._

  describe("Converting a template to Scala") {

    describe("which is empty") {

      it("produces a function returning an empty string") {
        new ScalaConverter().scala(Template()) should be(Right("""(tools: bhuj.rendering.Tools) => (context: bhuj.Context) => Right("")"""))
      }

    }

    describe("containing text components") {

      it("returns the text") {
        new ScalaConverter().scala(Template(Text("Hello!"))) should be(Right(s"""(tools: bhuj.rendering.Tools) => (context: bhuj.Context) => Right("" + \"\"\"Hello!\"\"\")"""))
      }

      it("escapes triple quotes in the text") {
        new ScalaConverter().scala(Template(Text(s""" \"\"\" + System.exit() + \"\"\" """))) should be(Right(s"""(tools: bhuj.rendering.Tools) => (context: bhuj.Context) => Right("" + \"\"\" \\"\\"\\" + System.exit() + \\"\\"\\" \"\"\")"""))
      }

      // TODO make sure we are within maximum string length for Scala / JVM (64KB?)
      // TODO what other things need to be escaped? Escaped chars?

    }

    describe("containing variable components") {

      it("returns the value from the context, escaped") {
        new ScalaConverter().scala(Template(Variable("key"))) should be(Right(s"""(tools: bhuj.rendering.Tools) => (context: bhuj.Context) => Right("" + context.get("key").map(value => tools.escapeHTML(value.toString)).getOrElse(""))"""))
      }

      it("does not escape the value for triple-delimited variables") {
        new ScalaConverter().scala(Template(TripleDelimitedVariable("key"))) should be(Right(s"""(tools: bhuj.rendering.Tools) => (context: bhuj.Context) => Right("" + context.getOrElse("key", ""))"""))
      }

      it("does not escape the value for ampersand-prefixed variables") {
        new ScalaConverter().scala(Template(AmpersandPrefixedVariable("key"))) should be(Right(s"""(tools: bhuj.rendering.Tools) => (context: bhuj.Context) => Right("" + context.getOrElse("key", ""))"""))
      }

    }

    describe("containing partials") {

      it("inserts the pre-rendered partial") {
        new ScalaConverter().scala(Template(Partial("template"))) should be(Right(s"""(tools: bhuj.rendering.Tools) => (context: bhuj.Context) => Right("" + tools.renderedPartial("template"))"""))
      }

    }

    describe("containing sections") {

      it("inserts the pre-rendered section") {
        val sectionTemplate = Template()
        new ScalaConverter().scala(Template(Section("section", sectionTemplate))) should be(Right(s"""(tools: bhuj.rendering.Tools) => (context: bhuj.Context) => Right("" + tools.renderedSection("section", ${sectionTemplate.hashCode()}))"""))
      }

    }

  }

}
