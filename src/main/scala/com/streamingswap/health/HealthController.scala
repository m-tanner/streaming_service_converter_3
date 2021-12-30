package com.streamingswap.health

import com.streamingswap.Settings
import play.api.Logger
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent._

class HealthController @Inject() (
  val controllerComponents: ControllerComponents,
  val settings: Settings,
) extends BaseController {

  val logger: Logger = Logger(this.getClass)

  private lazy val ONLINE_MESSAGE = "service is online"

  def index: Action[AnyContent] = Action {
    logger.debug(s"index $ONLINE_MESSAGE")
    Ok(ONLINE_MESSAGE)
  }

  def asyncIndex: Action[AnyContent] = Action.async {
    logger.debug("asyncIndex")
    Future.successful(Ok(ONLINE_MESSAGE))
  }
}

object HealthController {
  def apply(controllerComponents: ControllerComponents, settings: Settings): HealthController =
    new HealthController(controllerComponents, settings)
}
