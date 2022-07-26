package com.streamingswap.v1.statistics

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

import javax.inject.Inject

class StatisticsRouter @Inject() (controller: StatisticsController) extends SimpleRouter {
  val prefix = "/v1/statistics"

  override def routes: Routes = { case GET(p"/$id") =>
    controller.fetchStatisticsForPlaylist(id)
  }

}
