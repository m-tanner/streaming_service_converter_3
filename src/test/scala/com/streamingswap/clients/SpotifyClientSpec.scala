package com.streamingswap.clients

import com.streamingswap.models.{Item, Playlist}
import com.streamingswap.{PlaySpec, Settings}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.ws.ahc._
import play.api.mvc._
import play.api.{Environment, Mode}

import java.io.File
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.language.postfixOps

class SpotifyClientSpec extends PlaySpec with Results with GuiceOneAppPerSuite {

  private val settings                      = Settings.apply
  implicit val ec: ExecutionContextExecutor = global

  def withRealClient(testCode: SpotifyClient => Any): Any = {
    val environment = Environment(new File("."), this.getClass.getClassLoader, Mode.Test)
    val wsConfig    = AhcWSClientConfigFactory.forConfig(classLoader = environment.classLoader)
    val mat         = app.materializer
    val ws          = AhcWSClient(wsConfig)(mat)
    try {
      testCode(SpotifyClient(ws, settings)(ec)) // loan the client
    } finally ws.close()

  }

  "SpotifyClient" must {
    "successfully retrieve a bearer token" in withRealClient { client =>
      val futureResult: Future[List[(String, String)]] = client.authorize
      val result                                       = Await.result(futureResult, 1 second)
      result.head._2 must have length 90 // definitely a valid token
    }
    "successfully retrieve a playlist" in withRealClient { client =>
      val futureResult: Future[Playlist] = client.getPlaylist("720360kMd4LiSAVzyA8Ft4")
      val playlist                       = Await.result(futureResult, 1 second)
      playlist.name mustBe "Four Track Friday"
      playlist.owner mustBe Map("display_name" -> "Tanner")
      playlist.external_urls mustBe Map("spotify" -> "https://open.spotify.com/playlist/720360kMd4LiSAVzyA8Ft4")
    }
    "successfully get playlist tracks" in withRealClient { client =>
      val futureResult: Future[List[Item]] = client.getPlaylistTracks("720360kMd4LiSAVzyA8Ft4")
      val items                            = Await.result(futureResult, 10 second)
      items must have length 405
    }
  }
}
