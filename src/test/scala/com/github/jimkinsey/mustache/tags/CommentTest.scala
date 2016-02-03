package com.github.jimkinsey.mustache.tags

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class CommentTest extends FunSpec {

  describe("A comment tag") {

    it("starts with a bang") {
      Comment.pattern.findFirstIn("! comment") should be(defined)
    }

    it("may cross multiple lines") {
      Comment.pattern.findFirstIn(
        """!
          |comment
        """.stripMargin) should be(defined)
    }

    it("captures the comment text") {
      Comment.pattern.findFirstMatchIn("""! comment""").get.group(1) should be(" comment")
    }

    it("does not match when the string does not start with a bang") {
      Comment.pattern.findFirstIn("not a comment") should not be defined
    }

    it("is never processed") {
      Comment.process("comment", Map.empty, "remaining template", (x,y) => ???) should be(Right("" ->  "remaining template"))
    }

  }

}
