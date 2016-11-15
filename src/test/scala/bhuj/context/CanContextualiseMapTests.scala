package bhuj.context

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class CanContextualiseMapTests extends FunSpec {
  import canContextualiseMap.context

  describe("Contextualising a map") {

    it("returns an empty map if the map is empty") {
      context(Map[String,Any]()) should be(Right(Map.empty))
    }

    it("returns the map if non-empty") {
      context(Map("x" -> 42)) should be(Right(Map("x" -> 42)))
    }

    it("contextualises any case classes it finds") {
      case class Player(lives: Int)
      context(Map("player" -> Player(3))) should be(Right(Map("player" -> Map("lives" -> 3))))
    }

    it("returns the original value when it fails to contextualise a case class") {
      case class Uncontextualisable(i: Int) {
        val j = 9
      }
      context(Map("thing" -> Uncontextualisable(1))) should be(Right(Map("thing" -> Uncontextualisable(1))))
    }

    it("recursively contextualises nested maps") {
      case class Name(first: String)
      val map = Map("lead" -> Map("name" -> Name("Jim")))
      context(map) should be(Right(Map("lead" -> Map("name" -> Map("first" -> "Jim")))))
    }

    it("recursively contextualises case classes in lists") {
      case class Wall(height: Int)
      context(Map("walls" -> List(Wall(40)))) should be(Right(Map("walls" -> List(Map("height" -> 40)))))
    }

    it("recursively contextualises maps in lists") {
      case class Wall(height: Int)
      context(Map(
        "walls" -> List(
          Map("east" -> Wall(40)),
          Map("west" -> Wall(40)))
      )) should be(Right(Map(
        "walls" -> List(
          Map("east" -> Map("height" -> 40)),
          Map("west" -> Map("height" -> 40))))
      ))
    }

    it("recursively contextualises lists in lists") {
      case class Brick(length: Int)
      context(Map(
        "wall" -> List(List(Brick(2)))
      )) should be(Right(Map(
        "wall" -> List(List(Map("length" -> 2)))
      )))
    }

    it("contextualises a case class in an option") {
      case class House(name: String)
      context(Map(
        "house" -> Some(House("dunromin"))
      )) should be(Right(Map(
        "house" -> Some(Map("name" -> "dunromin"))
      )))
    }
  }

  private val caseClassConverter = new CaseClassConverter
  private val canContextualiseMap = new CanContextualiseMap(caseClassConverter)

}
