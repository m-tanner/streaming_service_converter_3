package com.streamingswap.v1.statistics

import com.streamingswap.spotify.{ PlaylistId, SpotifyClient }
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

  def fetchStatisticsForPlaylist(playlistId: String): Action[AnyContent] = Action {
    logger.debug("fetchStatisticsForPlaylist")
    val response = Try {
      val futureResult   = spotifyClient.fetchStatistics(PlaylistId(playlistId))
      val result: String = Await.result(futureResult, 10 seconds)
      result
    }
    response match {
      case Success(s) => Ok(s)
      case Failure(_) => InternalServerError("sorry")
    }
  }
}

object StatisticsController {
  def apply(spotifyClient: SpotifyClient, controllerComponents: ControllerComponents): StatisticsController =
    new StatisticsController(spotifyClient, controllerComponents)
}
