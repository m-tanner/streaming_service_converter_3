package com.streamingswap

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.ws.WSClient

import javax.inject.Inject
import scala.concurrent.ExecutionContext

abstract class Client @Inject() (ws: WSClient, settings: Settings)(implicit ec: ExecutionContext) extends LazyLogging {}
