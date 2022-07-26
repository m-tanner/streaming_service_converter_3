package com.streamingswap.v1.concerts

import com.streamingswap.PlaylistId
import com.streamingswap.bandsintown.BandsInTownClient
import com.streamingswap.spotify.SpotifyClient
import com.typesafe.scalalogging.LazyLogging
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{ Failure, Success, Try }

class ConcertsController @Inject() (
  val spotifyClient: SpotifyClient,
  val bandsInTownClient: BandsInTownClient,
  val controllerComponents: ControllerComponents,
) extends BaseController
    with LazyLogging {

  def fetchConcertsForPlaylist(id: String): Action[AnyContent] = Action {
    logger.info("fetchConcertsForPlaylist")

    val playlistId = PlaylistId(id)

    if (playlistId.hasNonAlphanumeric) {
      logger.info(s"fetchConcertsForPlaylist failed for invalid playlistId=$playlistId")
      BadRequest("not a valid spotify playlist id. these must be alphanumeric only.")
    } else {
      val response = Try {
        val futureResult   = bandsInTownClient.fetchConcerts(playlistId)
        val result: String = Await.result(futureResult, 10 seconds)
        result
      }

      response match {
        case Success(s) =>
          logger.info(s"fetchConcertsForPlaylist succeeded for playlistId=$playlistId")
          Ok(s)
        case Failure(_) =>
          logger.error(s"fetchConcertsForPlaylist failed for playlistId=$playlistId")
          InternalServerError("sorry, but we could not complete your request")
      }
    }
  }

}

object ConcertsController {
  def apply(
    spotifyClient: SpotifyClient,
    bandsInTownClient: BandsInTownClient,
    controllerComponents: ControllerComponents,
  ): ConcertsController =
    new ConcertsController(spotifyClient, bandsInTownClient, controllerComponents)
}
