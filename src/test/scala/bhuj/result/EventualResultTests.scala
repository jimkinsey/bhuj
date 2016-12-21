package bhuj.result

import bhuj.{TemplateNotFound, UnclosedTag}
import org.scalatest.AsyncFunSpec
import org.scalatest.Matchers._

import scala.concurrent.Future

class EventualResultTests extends AsyncFunSpec {
  import EventualResult._

  describe("An eventual result") {

    it("applies the function when flat-mapping on a Right") {
      EventualResult[Int](Future.successful(Right(6))).flatMap(res => EventualResult(Future.successful(Right(res * 7)))).future map (_ shouldBe Right(42))
    }

    it("applies the first function when folding on a Left") {
      EventualResult[Int](Future.successful(Left(TemplateNotFound("Boom")))).fold(_.asInstanceOf[TemplateNotFound].name.toUpperCase, identity) map (_ shouldBe Left("BOOM"))
    }

    it("applies the second function when folding on a Right") {
      EventualResult[Int](Future.successful(Right(42))).fold(identity, _ / 6) map (_ shouldBe Right(7))
    }

    it("can be built from an Either") {
      EventualResult.fromEither[Int](Right(42)).future map (_ shouldBe Right(42))
    }

    it("can be built from a Future of Option, putting the Some value on the Right") {
      EventualResult.fromFutureOption[Int](UnclosedTag("section"))(Future.successful(Some(42))).future map (_ shouldBe Right(42))
    }

    it("can be built from a Future of Option, using the provided function to create the Left when the value is None") {
      EventualResult.fromFutureOption[Int](UnclosedTag("section"))(Future.successful(None)).future map (_ shouldBe Left(UnclosedTag("section")))
    }

    it("can be used to chain nicely in a for comprehension") {
      val result: EventualResult[Int] = for {
        x <- Future.successful(Some(6)) |> fromFutureOption[Int](UnclosedTag("section1"))
        y <- Future.successful(Some(7)) |> fromFutureOption[Int](UnclosedTag("section12"))
      } yield { y * x }
      result.future map (_ shouldBe Right(42))
    }

    it("will fail fast, propagating the failure in a for comprehension") {
      val result: EventualResult[Int] = for {
        _ <- Future.successful(None) |> fromFutureOption(UnclosedTag("section1"))
        _ <- Future.successful(None) |> fromFutureOption(UnclosedTag("section2"))
      } yield { -1 }
      result.future map (_ shouldBe Left(UnclosedTag("section1")))
    }

    it("allows different types of failure with a common supertype in a for comprehension") {
      val result: EventualResult[Int] = for {
        _ <- Left(TemplateNotFound("flargle")) |> fromEither
        _ <- Left(UnclosedTag("flargle"))      |> fromEither
      } yield { -1 }
      result.future map (_ shouldBe Left(TemplateNotFound("flargle")))
    }

  }

}
