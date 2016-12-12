package bhuj.result

import scala.concurrent.{ExecutionContext, Future}

private[bhuj] case class EventualResult[Success,Failure](future: Future[Either[Failure,Success]]) {

  def flatMap[Success2](fn: Success => EventualResult[Success2,Failure])(implicit ec: ExecutionContext): EventualResult[Success2,Failure] = EventualResult(future.flatMap {
    case Right(s) => fn(s).future
    case Left(fail) => Future.successful(Left(fail))
  })

  def map[Success2](fn: Success => Success2)(implicit executionContext: ExecutionContext): EventualResult[Success2,Failure] = EventualResult(future.map {
    case Right(s) => Right(fn(s))
    case fail => fail.asInstanceOf[Left[Failure,Success2]]
  })

  def fold[Success2,Failure2](left: Failure => Failure2, right: Success => Success2)(implicit ec: ExecutionContext): Future[Either[Failure2,Success2]] = future map {
    _.left.map(left).right.map(right)
  }

}

private[bhuj] object EventualResult {
  import scala.language.implicitConversions

  def point[Success,Failure](s: Success): EventualResult[Success,Failure] = EventualResult(Future.successful(Right(s)))
  def fromEither[Failure,Success](either: Either[Failure,Success]) = EventualResult(Future.successful(either))
  def fromOption[Success,Failure](none: => Failure)(option: Option[Success]) = EventualResult(Future.successful(option.toRight(none)))
  def fromFutureOption[Failure,Success](none: => Failure)(futOption: Future[Option[Success]])(implicit ec: ExecutionContext) = EventualResult(futOption.map(_.toRight(none)))

  sealed class IdOps[A](self: A) {
    final def |>[B](f: A => B): B =
      f(self)
  }

  implicit def ToIdOps[A](a: A): IdOps[A] = new IdOps(a)
  implicit def ToResult[S,F](r: EventualResult[S,F])(implicit ec: ExecutionContext): Future[Either[F,S]] = r.fold(identity, identity)
}
