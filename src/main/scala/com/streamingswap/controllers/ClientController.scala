package com.streamingswap.controllers

import com.streamingswap.clients.SpotifyClient
import com.typesafe.scalalogging.LazyLogging
import play.api.mvc._

import javax.inject.Inject
import scala.language.postfixOps

class ClientController @Inject() (val spotifyClient: SpotifyClient, val controllerComponents: ControllerComponents)
    extends BaseController
    with LazyLogging {}

object ClientController {
  def apply(spotifyClient: SpotifyClient, controllerComponents: ControllerComponents): ClientController =
    new ClientController(spotifyClient, controllerComponents)
}
