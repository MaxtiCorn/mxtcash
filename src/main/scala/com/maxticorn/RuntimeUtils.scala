package com.maxticorn

import zio.Runtime
import zio.internal.Platform

import scala.concurrent.ExecutionContext

object RuntimeUtils {
  def apply[Env](env: Env, ec: ExecutionContext): Runtime[Env] = new Runtime[Env] {
    override val environment: Env   = env
    override val platform: Platform = Platform.fromExecutionContext(ec)
  }
}
