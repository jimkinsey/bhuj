package bhuj.rendering

class Tools(renderedPartials: Map[String, String], renderedSections: Map[Int, String]) {

  def renderedPartial(name: String): String = renderedPartials(name)

  def renderedSection(code: Int) = renderedSections(code)

  def escapeHTML(html: String) = html.foldLeft("") { case (acc, char) => acc + escapeCode.getOrElse(char, char) }

  private lazy val escapeCode = Map(
    '<' -> "&lt;",
    '>' -> "&gt;",
    '&' -> "&amp;",
    '\"' -> "&quot;",
    ''' -> "&#39;"
  )

}
