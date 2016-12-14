package bhuj

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object Benchmarking extends App {
  import MustacheBuilder._
  import bhuj.context.ContextImplicits._

  import scala.concurrent.ExecutionContext.Implicits.global

  case class NavItem(title: String, link: String, tag: Option[String] = None)
  case class Nav(items: Seq[NavItem])
  case class Item(title: String, content: String)
  case class Page(title: String, items: Seq[Item], nav: Nav)

  val context = Page(
    title = "Page Title",
    items = (1 to 100).map(i =>
      Item(
        title = s"Item $i",
        content = "<p>This is a paragraph.</p>")
      ),
    nav = Nav(
      items = Seq(
        NavItem(title = "Home", link = "/"),
        NavItem(title = "Contact", link = "/contact"),
        NavItem(title = "Away", link = "http://whatever.com/", tag = Some("external"))
      )
    )
  )

  val runs = 1000

  val mustache = mustacheRenderer
    .withTemplatePath(getClass.getClassLoader.getResource("templates").getPath)
    .withCache
    .withHelpers("localised" -> ((template, rendered) => Right("LOCALISED!!!")))

  def doRun() = Future { System.currentTimeMillis() } flatMap {
    runStart =>
      mustache.renderTemplate("page", context) map { res =>
        val runDuration = System.currentTimeMillis() - runStart
        (res, runDuration)
      }
  }

  val coldRun = Await.result(doRun(), Duration(10, "seconds"))
  println(s"Cold run took ${coldRun._2}ms")

  val start = System.currentTimeMillis()
  val results = Await.result(Future sequence (1 to runs).map(_ => doRun()), Duration(10, "seconds"))
  val duration = System.currentTimeMillis() - start
  val succeeded = results.count{
    case (Right(_), _) => true
    case _ => false
  }
  val totalDuration = results.foldLeft(0L)(_ + _._2)
  val avg = totalDuration.toFloat / runs.toFloat
  println(s"Run took real time of ${duration}ms, total time of ${totalDuration}ms (avg: ${avg}ms success: $succeeded/$runs)")

}
