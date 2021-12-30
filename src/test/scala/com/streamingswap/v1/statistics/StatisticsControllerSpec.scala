package com.streamingswap.v1.statistics

import com.streamingswap.spotify.SpotifyClient
import com.streamingswap.{ PlaySpec, Settings }
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.ws.ahc.{ AhcWSClient, AhcWSClientConfigFactory }
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import play.api.{ Environment, Mode }

import java.io.File
import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ ExecutionContextExecutor, Future }

class StatisticsControllerSpec extends PlaySpec with Results with GuiceOneAppPerSuite {

  private val settings                      = Settings.apply
  implicit val ec: ExecutionContextExecutor = global

  def withRealController(testCode: StatisticsController => Any): Any = {
    val environment   = Environment(new File("."), this.getClass.getClassLoader, Mode.Test)
    val wsConfig      = AhcWSClientConfigFactory.forConfig(classLoader = environment.classLoader)
    val mat           = app.materializer
    val ws            = AhcWSClient(wsConfig)(mat)
    val spotifyClient = SpotifyClient(ws, settings)(ec)
    try {
      testCode(StatisticsController(spotifyClient, Helpers.stubControllerComponents())) // loan the controller
    } finally ws.close()
  }

  "StatisticsController" must {
    "return valid 200 response" in withRealController { controller =>
      val result: Future[Result] = controller.fetchStatisticsForPlaylist("720360kMd4LiSAVzyA8Ft4").apply(FakeRequest())
      val json: String           = contentAsString(result)
      json must include("{\"tempo\":{\"name\":\"tempo\",\"minValue\":49.725,\"minAudioFeature\"")
      json must include(
        "\"info\":{\"name\":\"Four Track Friday\",\"owner\":{\"display_name\":\"Tanner\"},\"external_urls\":{\"spotify\":\"https://open.spotify.com/playlist/720360kMd4LiSAVzyA8Ft4\"}},"
      )
      json mustNot include("null")
    }
  }
}
