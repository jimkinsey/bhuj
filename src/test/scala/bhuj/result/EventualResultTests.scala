package bhuj.result

import org.scalatest.AsyncFunSpec
import org.scalatest.Matchers._

import scala.concurrent.Future

class EventualResultTests extends AsyncFunSpec {
  import EventualResult._

  describe("An eventual result") {

    it("applies the function when flat-mapping on a Right") {
      EventualResult[Int,Any](Future.successful(Right(6))).flatMap(res => EventualResult(Future.successful(Right(res * 7)))).future map (_ shouldBe Right(42))
    }

    it("applies the first function when folding on a Left") {
      EventualResult[Int,String](Future.successful(Left("boom"))).fold[Int,String](_.toUpperCase, identity) map (_ shouldBe Left("BOOM"))
    }

    it("applies the second function when folding on a Right") {
      EventualResult[Int,String](Future.successful(Right(42))).fold[Int,String](identity, _ / 6) map (_ shouldBe Right(7))
    }

    it("can be built from an Either") {
      EventualResult.fromEither[Any,Int](Right(42)).future map (_ shouldBe Right(42))
    }

    it("can be built from a Future of Option, putting the Some value on the Right") {
      EventualResult.fromFutureOption[String,Int]("")(Future.successful(Some(42))).future map (_ shouldBe Right(42))
    }

    it("can be built from a Futuer of Option, using the provided function to create the Left when the value is None") {
      EventualResult.fromFutureOption[String,Int]("Boom")(Future.successful(None)).future map (_ shouldBe Left("Boom"))
    }

    it("can be used to chain nicely in a for comprehension") {
      val result: EventualResult[Int, String] = for {
        x <- Future.successful(Some(6)) |> fromFutureOption[String,Int]("six :(")
        y <- Future.successful(Some(7)) |> fromFutureOption[String,Int]("seven :(")
      } yield { y * x }
      result.future map (_ shouldBe Right(42))
    }

    it("will fail fast, propagating the failure in a for comprehension") {
      val result: EventualResult[Int, String] = for {
        _ <- Future.successful(None) |> fromFutureOption("Fail #1")
        _ <- Future.successful(None) |> fromFutureOption("Fail #2")
      } yield { -1 }
      result.future map (_ shouldBe Left("Fail #1"))
    }

  }

}
