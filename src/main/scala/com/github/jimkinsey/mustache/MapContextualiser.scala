package com.github.jimkinsey.mustache

import com.github.jimkinsey.mustache.Contextualiser.Failure
import com.github.jimkinsey.mustache.MapContextualiser.MapContext

object MapContextualiser {
  type MapContext = Map[String, Any]
}

class MapContextualiser extends Contextualiser[MapContext] {

  def context(map: MapContext): Either[Failure, MapContext] = Right(map)

}
