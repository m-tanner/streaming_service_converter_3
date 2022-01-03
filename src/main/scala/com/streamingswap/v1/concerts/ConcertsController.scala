package com.streamingswap.v1.concerts

import com.streamingswap.spotify.{ PlaylistId, SpotifyClient }
import com.streamingswap.ticketmaster.TicketmasterClient
import com.typesafe.scalalogging.LazyLogging
import play.api.mvc._

import java.util.regex.Pattern
import javax.inject.Inject
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{ Failure, Success, Try }

class ConcertsController @Inject() (
  val spotifyClient: SpotifyClient,
  val ticketmasterClient: TicketmasterClient,
  val controllerComponents: ControllerComponents,
) extends BaseController
    with LazyLogging {

  def fetchConcertsForPlaylist(playlistId: String): Action[AnyContent] = Action {
    logger.info("fetchConcertsForPlaylist")

    val pattern            = Pattern.compile("[^a-zA-Z0-9]")
    val hasNonAlphanumeric = pattern.matcher(playlistId).find()

    if (hasNonAlphanumeric) {
      logger.info(s"fetchStatisticsForPlaylist failed for invalid playlistId=$playlistId")
      BadRequest("not a valid spotify playlist id. these must be alphanumeric only.")
    } else {
      val response = Try {
        val futureResult   = spotifyClient.fetchStatistics(PlaylistId(playlistId))
        val result: String = Await.result(futureResult, 10 seconds)
        result
      }

      response match {
        case Success(s) =>
          logger.info(s"fetchStatisticsForPlaylist succeeded for playlistId=$playlistId")
          Ok(s)
        case Failure(_) =>
          logger.error(s"fetchStatisticsForPlaylist failed for playlistId=$playlistId")
          InternalServerError("sorry, but we could not complete your request")
      }
    }
  }

}

object ConcertsController {
  def apply(
    spotifyClient: SpotifyClient,
    ticketmasterClient: TicketmasterClient,
    controllerComponents: ControllerComponents,
  ): ConcertsController = new ConcertsController(spotifyClient, ticketmasterClient, controllerComponents)
}
