package com.github.jimkinsey

import com.github.jimkinsey.Renderer.{Failure, Result, Context, Tag}



class Mustache extends Renderer(tags = Set(VariableTag, UnescapedVariableTag, SectionStartTag, InvertedSection, Comment))