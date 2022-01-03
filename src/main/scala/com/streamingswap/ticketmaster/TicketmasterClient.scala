package com.streamingswap.ticketmaster

import com.streamingswap.{ Client, ConcertClient, PlaylistId, Settings }
import play.api.libs.ws.WSClient

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class TicketmasterClient @Inject() (ws: WSClient, settings: Settings)(implicit ec: ExecutionContext)
    extends Client(ws, settings)(ec)
    with ConcertClient {

  override def fetchConcerts(id: PlaylistId): Future[String] = {
    Future.successful("ticketmaster")
  }

}

object TicketmasterClient {
  def apply(ws: WSClient, settings: Settings)(ec: ExecutionContext) = new TicketmasterClient(ws, settings)(ec)
}
