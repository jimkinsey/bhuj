package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.tags._

class Mustache extends Renderer(tags = Set(Variable, UnescapedVariable, SectionStart, InvertedSection, Comment))