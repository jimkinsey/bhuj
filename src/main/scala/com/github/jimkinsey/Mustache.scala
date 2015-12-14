package com.github.jimkinsey

class Mustache {
  def render(template: String, context: Map[String, Any] = Map.empty): String = {
    if (!template.contains("{{"))
      template
    else {
      val variableIndex = template.indexOf("{{")
      val remainder = template.substring(variableIndex + 2)
      val prefix = template.substring(0, variableIndex)
      val endOfVariableIndex: Int = remainder.indexOf("}}")
      val key = remainder.substring(0, endOfVariableIndex)
      val tail = remainder.substring(endOfVariableIndex + 2)

      prefix + context.getOrElse(key, "") + render(tail, context)
    }
  }
}