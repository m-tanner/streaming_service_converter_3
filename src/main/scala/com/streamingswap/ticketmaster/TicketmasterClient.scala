package com.streamingswap.ticketmaster

import com.streamingswap.Settings
import com.streamingswap.spotify.PlaylistId
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.ws.WSClient

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class TicketmasterClient @Inject() (ws: WSClient, settings: Settings)(implicit ec: ExecutionContext)
    extends LazyLogging {

  def fetchConcerts(id: PlaylistId): Future[String] = {
    Future.successful("yay")
  }

}

object TicketmasterClient {
  def apply(ws: WSClient, settings: Settings)(ec: ExecutionContext) = new TicketmasterClient(ws, settings)(ec)
}
