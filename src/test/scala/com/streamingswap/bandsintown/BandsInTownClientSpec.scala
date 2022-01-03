package com.streamingswap.bandsintown

import com.streamingswap.spotify.{Playlist, SpotifyClient}
import com.streamingswap.{PlaySpec, PlaylistId, Settings}
import org.scalatest.PrivateMethodTester
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.libs.json.Json.prettyPrint
import play.api.libs.ws.ahc._
import play.api.mvc._
import play.api.{Environment, Mode}

import java.io.File
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.language.postfixOps

class BandsInTownClientSpec extends PlaySpec with Results with PrivateMethodTester with GuiceOneAppPerSuite {

  private val settings                      = Settings.apply
  implicit val ec: ExecutionContextExecutor = global

  def withRealClient(testCode: BandsInTownClient => Any): Any = {
    val environment = Environment(new File("."), this.getClass.getClassLoader, Mode.Test)
    val wsConfig    = AhcWSClientConfigFactory.forConfig(classLoader = environment.classLoader)
    val mat         = app.materializer
    val ws          = AhcWSClient(wsConfig)(mat)
    val spotifyClient = SpotifyClient(ws, settings)(ec)
    try {
      testCode(BandsInTownClient(spotifyClient, ws, settings)(ec)) // loan the client
    } finally ws.close()
  }

  private val fourTrackFridayPlaylistId = PlaylistId("720360kMd4LiSAVzyA8Ft4")

  "BandsInTownClient" must {
    "successfully fetch events" in withRealClient { client =>
      val futureResult: Future[String] = client.fetchConcerts(fourTrackFridayPlaylistId)
      val json                         = Await.result(futureResult, 10 second)

      // for debugging
//      println(prettyPrint(Json.parse(json)))
      println(json)
    }
  }
}
