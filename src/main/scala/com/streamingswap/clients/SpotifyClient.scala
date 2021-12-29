package com.streamingswap.clients

import com.google.common.base.Charsets
import com.streamingswap.Settings
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.ws.WSClient

import java.util.Base64
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class SpotifyClient @Inject()(ws: WSClient, settings: Settings)(implicit ec: ExecutionContext) extends LazyLogging {
  private val spotifyAuth = settings.spotifyAuthUrl
  private val spotifyApi = settings.spotifyApiUrl

  def authorize(clientId: String, clientSecret: String): Future[String] = {

    val encodedIdAndSecret =
      Base64.getEncoder.encodeToString(s"$clientId:$clientSecret".getBytes(Charsets.UTF_8))

    ws.url(s"$spotifyAuth/api/token")
      .withHttpHeaders(
        "Authorization" -> s"Basic $encodedIdAndSecret",
        "Content-Type" -> "application/x-www-form-urlencoded"
      )
      .post(Map("grant_type" -> "client_credentials"))
      .map { response =>
        (response.json \ "access_token").as[String]
      }
  }

  def getTracks(tracks: String): Future[String] = {
    val token = "BQCC0R7jI3mExDS5QKf5FHOccO0DptcyVsm3ndiXVaqyDkJHPYgMMYfPrc5lBhrnVraI3MNKZ9NN8A9EBjU"

    ws.url(s"$spotifyApi/v1/tracks/$tracks")
      .withHttpHeaders(
        "Authorization" -> s"Bearer $token"
      )
      .get()
      .map { response => response.body }
  }
}

object SpotifyClient {
  def apply(ws: WSClient, settings: Settings)(ec: ExecutionContext) =
    new SpotifyClient(ws, settings)(ec)
}
