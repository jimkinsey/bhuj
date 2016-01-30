package com.github.jimkinsey.mustache

import org.scalatest.FunSpec
import org.scalatest.Matchers._

import scala.util.Random

class CachingTests extends FunSpec {
  import Caching.cached

  describe("Adding a cache to a function") {

    it("invokes the function the first time") {
      val timesTwo: Int => Int = _ * 2
      cached(timesTwo)(2) should be(4)
    }

    it("does not invoke the function the second time") {
      val randMult: Int => Int = cached(_ * Random.nextInt())
      val firstResult = randMult(3)
      val secondResult = randMult(3)
      secondResult should be(firstResult)
    }

  }

}
