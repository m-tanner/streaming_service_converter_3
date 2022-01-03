package com.streamingswap.v1.statistics

import com.streamingswap.PlaylistId
import com.streamingswap.spotify.SpotifyClient
import com.typesafe.scalalogging.LazyLogging
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{ Failure, Success, Try }

class StatisticsController @Inject() (val spotifyClient: SpotifyClient, val controllerComponents: ControllerComponents)
    extends BaseController
    with LazyLogging {

  def fetchStatisticsForPlaylist(id: String): Action[AnyContent] = Action {
    logger.info("fetchStatisticsForPlaylist")

    val playlistId = PlaylistId(id)

    if (playlistId.hasNonAlphanumeric) {
      logger.info(s"fetchStatisticsForPlaylist failed for invalid playlistId=$playlistId")
      BadRequest("not a valid spotify playlist id. these must be alphanumeric only.")
    } else {
      val response = Try {
        val futureResult   = spotifyClient.fetchStatistics(playlistId)
        val result: String = Await.result(futureResult, 10 seconds)
        result
      }

      logMemory() // TODO this is for debugging, remove when able

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

  private def logMemory(): Unit = {
    // memory info
    val mb      = 1024 * 1024
    val runtime = Runtime.getRuntime
    logger.debug("** ALL RESULTS IN MB **")
    logger.debug("** Used Memory:  " + (runtime.totalMemory - runtime.freeMemory) / mb)
    logger.debug("** Free Memory:  " + runtime.freeMemory / mb)
    logger.debug("** Total Memory: " + runtime.totalMemory / mb)
    logger.debug("** Max Memory:   " + runtime.maxMemory / mb)
  }

}

object StatisticsController {
  def apply(spotifyClient: SpotifyClient, controllerComponents: ControllerComponents): StatisticsController =
    new StatisticsController(spotifyClient, controllerComponents)
}
