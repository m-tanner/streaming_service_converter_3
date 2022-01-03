package com.streamingswap.spotify

import com.google.common.base.Charsets
import com.streamingswap.{ Client, PlaylistId, Settings }
import org.json4s.DefaultFormats
import org.json4s.native.{ Json => Json4s }
import play.api.libs.json.{ Json, Reads }
import play.api.libs.ws.WSClient

import java.util.Base64
import javax.inject.Inject
import scala.annotation.tailrec
import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.language.{ implicitConversions, postfixOps }

class SpotifyClient @Inject() (ws: WSClient, settings: Settings)(implicit ec: ExecutionContext)
    extends Client(ws, settings)(ec) {

  def fetchStatistics(id: PlaylistId): Future[String] = {
    for {
      info       <- fetchPlaylistInfo(id)
      statistics <- buildStats(id)
    } yield Json4s(DefaultFormats).write(statistics + ("info" -> info))
  }

  private def fetchPlaylistInfo(id: PlaylistId): Future[Playlist] = authorize flatMap { authHeader =>
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

  private def buildStats(id: PlaylistId): Future[Map[String, Stat]] = {

    val facets = List(
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

    val result = for {
      tracks   <- fetchTracks(id)
      features <- fetchAudioFeaturesByTrack(tracks.values.toList)
    } yield {
      // initialize the accumulator
      // having the name in both places, key and stat.name, is useful for debugging
      var statsAccumulator: mutable.Map[String, Stat] =
        facets.foldLeft(mutable.Map[String, Stat]())((theMap, thisStatName) =>
          theMap + (thisStatName -> Stat(name = thisStatName)) // TODO fix this compiler warning
        )

      for (metric <- facets) {
        for (feature <- features.values) {
          val currentFeatureValue = feature.extract(metric)
          val currentMetricValue  = statsAccumulator(metric)
          if (currentFeatureValue <= currentMetricValue.minValue) {
            statsAccumulator(metric) =
              currentMetricValue.copy(minValue = currentFeatureValue, minAudioFeature = Some(feature))
          }
          if (currentFeatureValue >= currentMetricValue.maxValue) {
            statsAccumulator(metric) =
              currentMetricValue.copy(maxValue = currentFeatureValue, maxAudioFeature = Some(feature))
          }
          val previousSum = currentMetricValue.sum
          statsAccumulator.update(metric, statsAccumulator(metric).copy(sum = previousSum + currentFeatureValue))
        }

        val resultingMetricsStats = statsAccumulator(metric)
        statsAccumulator.update(
          metric,
          resultingMetricsStats.copy(
            minTrack = Some(tracks(resultingMetricsStats.minAudioFeature.get.id)),
            maxTrack = Some(tracks(resultingMetricsStats.maxAudioFeature.get.id)),
            avg = resultingMetricsStats.sum / features.size,
          ),
        )
      }
      statsAccumulator.toMap // remove mutability
    }

    result
  }

  def fetchTracks(id: PlaylistId): Future[Map[String, Track]] = {
    fetchPlaylistItems(id).map { items =>
      items.foldLeft(Map[String, Track]())((m, s) => m + (s.track.id -> s.track)) // repackage as a map of (id -> track)
    }
  }

  private def fetchPlaylistItems(id: PlaylistId): Future[List[Item]] = authorize flatMap { authHeader =>
    @tailrec
    def recursiveGetPlaylistItems(
      url: String,
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
        recursiveGetPlaylistItems(nextUrl, queryStringParams, items ++ playlistTracks.items)
      }
    }

    val queryStringParams = List[(String, String)](
      "fields" -> """limit,next,offset,total,href,items(added_at,track(album(name,release_date,images,album_type),artists(name,id),name,release_date,duration_ms,explicit,external_urls,popularity,id))"""
    )

    recursiveGetPlaylistItems(s"${settings.spotifyApiUrl}/v1/playlists/$id/tracks", queryStringParams)
  }

  private def fetchAudioFeaturesByTrack(tracks: List[Track]): Future[Map[String, AudioFeature]] = authorize flatMap {
    authHeader =>
      @tailrec
      def recursiveGetAudioFeatures(
        remainingTracks: List[Track],
        limit: Int = 100,
        acc: Map[String, AudioFeature] = Map[String, AudioFeature](),
      ): Future[Map[String, AudioFeature]] = {

        implicit val audioFeatureReads: Reads[AudioFeature] = Json.reads[AudioFeature]

        if (remainingTracks.isEmpty) {
          Future.successful(acc)
        } else {
          val ids               = remainingTracks.take(limit).map(_.id)
          val queryStringParams = List[(String, String)]("ids" -> ids.mkString(","))

          val futureAudioFeatures = for {
            response <- ws
                          .url(s"${settings.spotifyApiUrl}/v1/audio-features")
                          .withHttpHeaders(authHeader: _*) // vararg compatibility
                          .withQueryStringParameters(queryStringParams: _*)
                          .get()
            audioFeatures = response.json.as[AudioFeatures](Json.reads[AudioFeatures])
          } yield audioFeatures

          val audioFeatures = Await.result(futureAudioFeatures, 5 seconds) // TODO eliminate this await

          recursiveGetAudioFeatures(
            remainingTracks.drop(limit),
            limit,
            acc ++ audioFeatures.audio_features.map(feature => (feature.id, feature)),
          )
        }
      }

      for {
        audioFeatures <- recursiveGetAudioFeatures(tracks)
      } yield audioFeatures
  }

  private def authorize: Future[List[(String, String)]] = {

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
}

object SpotifyClient {
  def apply(ws: WSClient, settings: Settings)(ec: ExecutionContext) = new SpotifyClient(ws, settings)(ec)
}
