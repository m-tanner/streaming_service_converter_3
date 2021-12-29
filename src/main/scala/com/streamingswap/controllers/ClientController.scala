package com.streamingswap.controllers

import com.streamingswap.clients.SpotifyClient
import com.typesafe.scalalogging.LazyLogging
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class ClientController @Inject()(val spotifyClient: SpotifyClient, val controllerComponents: ControllerComponents) extends BaseController with LazyLogging {

  def authorizeSpotify(clientId: String, clientSecret: String): Action[AnyContent] = Action {
    logger.debug(s"Called authorizeSpotify")
    val result = Await.result(spotifyClient.authorize(clientId, clientSecret), 1 second)
    Ok(result)
  }

  def getTracks(tracks: String = "2TpxZ7JUBn3uw46aR7qd6V"): Action[AnyContent] = Action {
    val result = Await.result(spotifyClient.getTracks(tracks), 1 second)
    Ok(result)
  }
}

object ClientController {
  def apply(spotifyClient: SpotifyClient, controllerComponents: ControllerComponents): ClientController =
    new ClientController(spotifyClient, controllerComponents)
}
