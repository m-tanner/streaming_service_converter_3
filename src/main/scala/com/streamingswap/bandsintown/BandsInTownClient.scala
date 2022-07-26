package com.streamingswap.bandsintown

import com.streamingswap.spotify.{Artist, Playlist, SpotifyClient, Track}
import com.streamingswap.{Client, ConcertClient, PlaylistId, Settings}
import play.api.libs.json.{Json, Reads}
import play.api.libs.ws.WSClient

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

class BandsInTownClient @Inject() (spotifyClient: SpotifyClient, ws: WSClient, settings: Settings)(implicit ec: ExecutionContext)
    extends Client(ws, settings)(ec)
    with ConcertClient {

  override def fetchConcerts(id: PlaylistId): Future[String] = {

    val futureResult   = spotifyClient.fetchTracks(id)
    val result: Map[String, Track] = Await.result(futureResult, 10 seconds)
    val tracks: List[Track] = result.values.toList
    val artists: List[Artist] = tracks.flatMap(track => track.artists)
    val artistNames: List[String] = artists.map(artist => artist.name)

    val futureEvents = fetchUpcomingShows(artistNames.head)
    val events = Await.result(futureEvents, 10 seconds)

    Future.successful(events.toString)
  }

  private def fetchUpcomingShows(artistName: String): Future[Option[Events]] = {
    val headers = List[(String, String)](
      "Accept" -> "application/json",
    )
    val queryStringParams = List[(String, String)](
      "app_id" -> settings.bandsInTownAppId,
      "date" -> "upcoming",
    )

    implicit val venueReads: Reads[Venue] = Json.reads[Venue]
    implicit val eventReads: Reads[Event] = Json.reads[Event]

    val stop = "hi"

    // TODO here is where jason, christian, and I stopped
    // TODO i need to figure out why the api isn't coming back with any data
    // TODO then i need to figure out if the response matches my model
    // TODO then i need to get results for all artists in a playlist
    // TODO then i need to shape that into json usable by a UI

    for {
      response <- ws
        .url(s"${settings.bandsInTownUrl}/artists/Justin%20Bieber/events")
        .withHttpHeaders(headers: _*)
        .withQueryStringParameters(queryStringParams: _*)
        .get()
      events = response.json.asOpt[Events](Json.reads[Events])
    } yield {
        val stop2 = "stop"
        events
      }
  }

}

object BandsInTownClient {
  def apply(spotifyClient: SpotifyClient, ws: WSClient, settings: Settings)(ec: ExecutionContext) = new BandsInTownClient(spotifyClient, ws, settings)(ec)
}
