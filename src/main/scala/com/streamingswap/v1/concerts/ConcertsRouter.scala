package com.streamingswap.v1.concerts

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

import javax.inject.Inject

class ConcertsRouter @Inject() (controller: ConcertsController) extends SimpleRouter {
  val prefix = "/v1/statistics"

  override def routes: Routes = { case GET(p"/$id") =>
    controller.fetchConcertsForPlaylist(id)
  }

}
