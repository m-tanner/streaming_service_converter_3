package com.streamingswap.v1.concerts

import com.streamingswap.bandsintown.BandsInTownClient
import com.streamingswap.spotify.SpotifyClient
import com.streamingswap.{PlaySpec, Settings}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.ws.ahc.{AhcWSClient, AhcWSClientConfigFactory}
import play.api.mvc._
import play.api.test._
import play.api.{Environment, Mode}

import java.io.File
import scala.concurrent.ExecutionContext.global
import scala.concurrent.ExecutionContextExecutor

class ConcertsControllerSpec extends PlaySpec with Results with GuiceOneAppPerSuite {

  private val settings                      = Settings.apply
  implicit val ec: ExecutionContextExecutor = global

  def withRealController(testCode: ConcertsController => Any): Any = {
    val environment        = Environment(new File("."), this.getClass.getClassLoader, Mode.Test)
    val wsConfig           = AhcWSClientConfigFactory.forConfig(classLoader = environment.classLoader)
    val mat                = app.materializer
    val ws                 = AhcWSClient(wsConfig)(mat)
    val spotifyClient      = SpotifyClient(ws, settings)(ec)
    val bandsInTownClient = BandsInTownClient(spotifyClient, ws, settings)(ec)
    try {
      testCode(
        ConcertsController(spotifyClient, bandsInTownClient, Helpers.stubControllerComponents())
      ) // loan the controller
    } finally ws.close()
  }

  "ConcertsController" must {
    "do nothing" in withRealController(controller => println("hi"))
  }
}
