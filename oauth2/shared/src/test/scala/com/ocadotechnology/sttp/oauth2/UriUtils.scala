package org.polyvariant.sttp.oauth2

import sttp.model.Uri

object UriUtils {
  private def tupleToParam(k: String, v: String) = s"$k=$v"

  def expectedResult(baseUri: Uri, path: AuthorizationCodeProvider.Config.Path, params: List[(String, String)]): String = {
    val paramsSubstring = params.map((tupleToParam _).tupled).mkString("&")
    s"${baseUri.addPath(path.values).toString()}?$paramsSubstring"
  }

}
