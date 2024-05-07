package org.polyvariant.sttp.oauth2

import org.polyvariant.sttp.oauth2.common.Scope
import org.polyvariant.sttp.oauth2.common.ValidScope
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import eu.timepit.refined._

class ValidScopeTest extends AnyWordSpec with Matchers {

  val allowedChars: List[Char] = 33.toChar +: List(' ') ::: (35 to 91).map(_.toChar).toList ::: (93 to 125).map(_.toChar).toList

  "Scope" should {
    "be created according to RFC allowed characters" in {
      assert(
        refineV[ValidScope](allowedChars.mkString("")).toOption ===
          Scope.of("! #$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_`abcdefghijklmnopqrstuvwxyz{|}")
      )
    }

    "not be created for empty string" in {
      assert(refineV[ValidScope]("") === Left("""Predicate failed: "" matches ValidScope."""))
    }

    "not be created for characters outside allowed range" in {
      assert(refineV[ValidScope](""" "\""") === Left("""Predicate failed: " "\" matches ValidScope."""))
    }
  }

}
