package com.streamingswap.clients

import com.google.common.base.Charsets
import com.streamingswap.Settings
import com.streamingswap.models._
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.{ Json, Reads }
import play.api.libs.ws.WSClient

import java.util.Base64
import javax.inject.Inject
import scala.annotation.tailrec
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.language.{ implicitConversions, postfixOps }

class SpotifyClient @Inject() (ws: WSClient, settings: Settings)(implicit ec: ExecutionContext) extends LazyLogging {

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

  def getPlaylistItems(id: String): Future[List[Item]] = authorize flatMap { authHeader =>
    val queryStringParams = List[(String, String)](
      "fields" -> """limit,next,offset,total,href,items(added_at,track(album(name,release_date,images,album_type),artists(name,id),name,release_date,duration_ms,explicit,external_urls,popularity,id))"""
    )

    recursiveGetPlaylistTracks(s"${settings.spotifyApiUrl}/v1/playlists/$id/tracks", authHeader, queryStringParams)
  }

  def getPlaylistTracks(id: String): Future[Map[String, Track]] = {
    getPlaylistItems(id).map { items =>
      items.foldLeft(Map[String, Track]())((m, s) => m + (s.track.id -> s.track))
    }
  }

  def getAudioFeatures(playlistId: String): Future[Map[String, AudioFeature]] = authorize flatMap { authHeader =>
    for {
      tracks   <- getPlaylistTracks(playlistId)
      features <- recursiveGetAudioFeatures(authHeader, tracks.values.toList)
    } yield features
  }

  def getFeatureMetrics(playlistId: String): Future[List[String]] = {

    def extract(feature: AudioFeature, field: String): Float = {
      field match {
        case "acousticness"     => feature.acousticness
        case "danceability"     => feature.danceability
        case "duration_ms"      => feature.duration_ms.toFloat
        case "energy"           => feature.energy
        case "instrumentalness" => feature.instrumentalness
        case "liveness"         => feature.liveness
        case "loudness"         => feature.loudness
        case "tempo"            => feature.tempo
        case "valence"          => feature.valence
      }
    }

    val ofInterest = List(
      "acousticness",
      "danceability",
      "duration_ms",
      "energy",
      "instrumentalness",
      "liveness",
      "loudness",
      "tempo",
      "valence",
    )

    val metrics = Map[String, Stat]()

    // iterate through the features for each metric

    for {
      tracks   <- getPlaylistTracks(playlistId)
      features <- getAudioFeatures(playlistId)
    } yield {}

    Future.successful(List[String]())
  }

  @tailrec
  private def recursiveGetAudioFeatures(
    authHeader: List[(String, String)],
    tracks: List[Track],
    limit: Int = 100,
    acc: Map[String, AudioFeature] = Map[String, AudioFeature](),
  ): Future[Map[String, AudioFeature]] = {

    implicit val audioFeatureReads: Reads[AudioFeature] = Json.reads[AudioFeature]

    if (tracks.isEmpty) {
      Future.successful(acc)
    } else {
      val ids               = tracks.take(limit).map(_.id)
      val queryStringParams = List[(String, String)]("ids" -> ids.mkString(","))

      val futureAudioFeatures = for {
        response <- ws
                      .url(s"${settings.spotifyApiUrl}/v1/audio-features")
                      .withHttpHeaders(authHeader: _*) // vararg compatibility
                      .withQueryStringParameters(queryStringParams: _*)
                      .get()
        audioFeatures = response.json.as[AudioFeatures](Json.reads[AudioFeatures])
      } yield audioFeatures

      val audioFeatures = Await.result(futureAudioFeatures, 5 seconds)

      recursiveGetAudioFeatures(
        authHeader,
        tracks.drop(limit),
        limit,
        acc ++ audioFeatures.audio_features.map(feature => (feature.id, feature)),
      )
    }
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
