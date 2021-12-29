package com.streamingswap.clients

import com.google.common.base.Charsets
import com.streamingswap.Settings
import com.streamingswap.models._
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.{Json, Reads}
import play.api.libs.ws.WSClient

import java.util.Base64
import javax.inject.Inject
import scala.annotation.tailrec
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.{implicitConversions, postfixOps}

class SpotifyClient @Inject() (ws: WSClient, settings: Settings)(implicit ec: ExecutionContext) extends LazyLogging {

  private val desiredFeatures = Set(
    "acousticness",
    "danceability",
    "duration_ms",
    "energy",
    "instrumentalness",
    "liveness",
    "loudness",
    "tempo",
    "popularity",
    "valence",
  )

  def authorize: Future[List[(String, String)]] = {

    val encodedCredentials =
      Base64.getEncoder
        .encodeToString(
          s"${settings.spotifyClientId}:${settings.spotifyClientSecret}".getBytes(Charsets.UTF_8)
        )

    val authHeaders = List[(String, String)](
      "Authorization" -> s"Basic $encodedCredentials",
      "Content-Type"  -> "application/x-www-form-urlencoded",
    )

    for {
      response <- ws
                    .url(s"${settings.spotifyAuthUrl}/api/token")
                    .withHttpHeaders(authHeaders: _*) // vararg compatibility
                    .post(Map("grant_type" -> "client_credentials"))
      creds = response.json.as[ClientCredentials](Json.reads[ClientCredentials])
    } yield List[(String, String)]("Authorization" -> s"Bearer ${creds.access_token}")
  }

  def getPlaylist(id: String): Future[Playlist] = authorize flatMap { authHeader =>
    val queryStringParams = List[(String, String)]("fields" -> "name,owner(display_name),external_urls(spotify)")

    for {
      response <- ws
                    .url(s"${settings.spotifyApiUrl}/v1/playlists/$id")
                    .withHttpHeaders(authHeader: _*) // vararg compatibility
                    .withQueryStringParameters(queryStringParams: _*)
                    .get()
      playlist = response.json.as[Playlist](Json.reads[Playlist])
    } yield playlist
  }

  def getPlaylistTracks(id: String): Future[List[Item]] = authorize flatMap { authHeader =>
    val queryStringParams = List[(String, String)](
      "fields" -> """limit,next,offset,total,href,items(added_at,track(album(name,release_date,images,album_type),artists(name,id),name,release_date,duration_ms,explicit,external_urls,popularity,id))"""
    )

    recursiveGetPlaylistTracks(s"${settings.spotifyApiUrl}/v1/playlists/$id/tracks", authHeader, queryStringParams)
  }

  @tailrec
  private def recursiveGetPlaylistTracks(
    url: String,
    authHeader: List[(String, String)],
    queryStringParams: List[(String, String)],
    items: List[Item] = List(),
  ): Future[List[Item]] = {

    implicit val artistReads: Reads[Artist] = Json.reads[Artist]
    implicit val imageReads: Reads[Image]   = Json.reads[Image]
    implicit val albumReads: Reads[Album]   = Json.reads[Album]
    implicit val trackReads: Reads[Track]   = Json.reads[Track]
    implicit val itemReads: Reads[Item]     = Json.reads[Item]

    val futurePlaylistTracks = for {
      response <- ws
                    .url(url)
                    .withHttpHeaders(authHeader: _*)                  // vararg compatibility
                    .withQueryStringParameters(queryStringParams: _*) // vararg compatibility
                    .get()
      playlistTracks = response.json.as[PlaylistTracks](Json.reads[PlaylistTracks]) // needs the above implicits
    } yield playlistTracks

    val playlistTracks = Await.result(futurePlaylistTracks, 5 seconds) // TODO eliminate this await

    if (playlistTracks.next.getOrElse("").isBlank) { // the api uses null
      Future.successful(items ++ playlistTracks.items)
    } else {
      val nextUrl = playlistTracks.next.get // we know it exists here
      recursiveGetPlaylistTracks(nextUrl, authHeader, queryStringParams, items ++ playlistTracks.items)
    }
  }
}

object SpotifyClient {
  def apply(ws: WSClient, settings: Settings)(ec: ExecutionContext) = new SpotifyClient(ws, settings)(ec)
}
