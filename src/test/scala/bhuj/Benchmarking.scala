package bhuj

object Benchmarking extends App {
  import MustacheBuilder._
  import bhuj.context.ContextImplicits._

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
  def doRun() =  mustache.renderTemplate("page", context)
  val coldStart = System.currentTimeMillis()
  val coldRun = doRun
  val coldDuration = System.currentTimeMillis() - coldStart
  println(s"Cold run took ${coldDuration}ms")
  val start = System.currentTimeMillis()
  val results = (1 to runs).map(_ => {
    doRun()
  })
  val duration = System.currentTimeMillis() - start
  val avg = duration.toFloat / runs.toFloat
  val succeeded = results.count{
    case Right(_) => true
    case _ => false
  }

  println(s"Run took ${duration}ms (avg: $avg success: $succeeded/$runs)")

}
