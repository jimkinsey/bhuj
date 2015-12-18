package com.github.jimkinsey

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class MustacheTest extends FunSpec {

  describe("Mustache") {

    it("leaves a string containing no tags untouched") {
      new Mustache().render("No tags") should be("No tags")
    }

    it("leaves an empty string untouched") {
      new Mustache().render("") should be("")
    }

    it("works for a multi-line template") {
      new Mustache().render(
        """1: {{one}},
          |2: {{two}},
          |3: {{three}}""".stripMargin, Map("one" -> 1, "two" -> 2, "three" -> 3)) should be(
        """1: 1,
          |2: 2,
          |3: 3""".stripMargin)
    }

    describe("a variable tag") {

      it("is replaced by an empty string when the key is not in the context") {
        new Mustache().render("Hello {{name}}", Map.empty) should be("Hello ")
      }

      it("is replaced by the value from the context when present") {
        new Mustache().render("Hello {{name}}", Map("name" -> "Chris")) should be("Hello Chris")
      }

      it("works with multiple variables") {
        new Mustache().render("Hi {{first}} {{last}}", Map("first" -> "John", "last" -> "Smith")) should be("Hi John Smith")
      }

      it("escapes for HTML by default") {
        new Mustache().render("{{html}}", Map("html" -> """<blink>"&'</blink>""")) should be("&lt;blink&gt;&quot;&amp;&#39;&lt;/blink&gt;")
      }

      it("does not escape when the variable is triple-delimited") {
        new Mustache().render("{{{html}}}", Map("html" -> """<blink>"&'</blink>""")) should be("""<blink>"&'</blink>""")
      }

      it("may have a one character name") {
        new Mustache().render("{{x}}", Map("x" -> "X")) should be("""X""")
      }

      it("may have a one character name for a non-escaped variable") {
        new Mustache().render("{{{x}}}", Map("x" -> "X")) should be("""X""")
      }

    }

    describe("a section tag") {

      describe("for a false value") {

        it("does not render") {
          new Mustache().render(
            """Shown.
              |{{#person}}
              |  Never shown!
              |{{/person}}""".stripMargin, Map("person" -> false)) should be(
            """Shown.
              |""".stripMargin
          )
        }

      }

      describe("for an empty iterable") {

        it("does not render") {
          new Mustache().render(
            """Shown.
              |{{#person}}
              |  Never shown!
              |{{/person}}""".stripMargin, Map("person" -> Seq.empty)) should be(
            """Shown.
              |""".stripMargin
          )
        }

      }

    }

  }

}