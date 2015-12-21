package com.github.jimkinsey.mustache

import tags._

class Mustache(partials: Map[String,String] = Map.empty) extends Renderer(tags = Set(
  Variable,
  UnescapedVariable,
  SectionStart,
  InvertedSection,
  Comment,
  new Partial(partials)))