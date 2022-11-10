package com.ocadotechnology.sttp.oauth2

import scala.concurrent.ExecutionContext
import org.scalatest.AsyncTestSuite

trait CrossPlatformAsyncTestSuite { self: AsyncTestSuite =>
  implicit override val executionContext: ExecutionContext = org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global
}
