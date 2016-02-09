package com.github.jimkinsey.mustache

object Benchmarking extends App {
  import MustacheBuilder._
  import com.github.jimkinsey.mustache.context.ContextImplicits._

  val context: Context = Map(
    "title" -> "Page Title",
    "content" ->
      """<p>This is a paragraph.</p>
        |<p>This is a paragraph.</p>
        |<p>This is a paragraph.</p>
        |<p>This is a paragraph.</p>
        |<p>This is a paragraph.</p>
        |<p>This is a paragraph.</p>
        |<p>This is a paragraph.</p>
      """.stripMargin,
    "nav" -> Map(
      "items" -> Seq(
        Map("title" -> "Home", "link" -> "/"),
        Map("title" -> "Contact", "link" -> "/contact"),
        Map("title" -> "Away", "link" -> "http://whatever.com/", "tag" -> Some("external"))
      )
    )
  )
  val runs = 1000
  val mustache = mustacheRenderer
    .withTemplatePath(getClass.getClassLoader.getResource("templates").getPath)
    .withCache
    .withHelpers("localised" -> ((template, rendered) => Right("LOCALISED!!!")))
  val start = System.currentTimeMillis()
  val results = (1 to runs).map(_ => {
    mustache.renderTemplate("page", context)
  })
  val duration = System.currentTimeMillis() - start
  val avg = duration.toFloat / runs.toFloat
  val succeeded = results.count{
    case Right(_) => true
    case _ => false
  }

  println(s"Run took ${duration}ms (avg: $avg success: $succeeded/$runs)")

}
