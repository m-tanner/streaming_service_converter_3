package com.streamingswap

import scala.concurrent.Future

trait ConcertClient {
  def fetchConcerts(id: PlaylistId): Future[String] = ??? // json
}
