package org.polyvariant.sttp.oauth2

import org.polyvariant.sttp.oauth2.common.Scope

sealed trait ScopeSelection extends Product with Serializable {

  def toRequestMap: Map[String, String] = this match {
    case ScopeSelection.KeepExisting         => Map.empty
    case ScopeSelection.OverrideWith(scopes) => Map("scopes" -> scopes.mkString(" "))
  }

}

object ScopeSelection {
  case object KeepExisting extends ScopeSelection
  final case class OverrideWith(scopes: Set[Scope]) extends ScopeSelection
}
