package com.streamingswap.spotify

import com.streamingswap.{ PlaySpec, PlaylistId, Settings }
import org.scalatest.PrivateMethodTester
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.libs.json.Json.prettyPrint
import play.api.libs.ws.ahc._
import play.api.mvc._
import play.api.{ Environment, Mode }

import java.io.File
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, ExecutionContextExecutor, Future }
import scala.language.postfixOps

class SpotifyClientSpec extends PlaySpec with Results with PrivateMethodTester with GuiceOneAppPerSuite {

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

  private val fourTrackFridayPlaylistId = PlaylistId("720360kMd4LiSAVzyA8Ft4")

  "SpotifyClient" must {
    "successfully retrieve a bearer token" in withRealClient { client =>
      val authorize = PrivateMethod[Future[List[(String, String)]]](Symbol("authorize"))

      val futureResult: Future[List[(String, String)]] = client invokePrivate authorize()
      val result                                       = Await.result(futureResult, 1 second)
      result.head._2 must have length 90 // definitely a valid token
    }
    "successfully retrieve a playlist" in withRealClient { client =>
      val fetchPlaylistInfo = PrivateMethod[Future[Playlist]](Symbol("fetchPlaylistInfo"))

      val futureResult: Future[Playlist] = client invokePrivate fetchPlaylistInfo(fourTrackFridayPlaylistId)
      val playlist                       = Await.result(futureResult, 1 second)
      playlist.name mustBe "Four Track Friday"
      playlist.owner mustBe Map("display_name" -> "Tanner")
      playlist.external_urls mustBe Map("spotify" -> "https://open.spotify.com/playlist/720360kMd4LiSAVzyA8Ft4")
    }
    "successfully get playlist items" in withRealClient { client =>
      val fetchPlaylistItems = PrivateMethod[Future[List[Item]]](Symbol("fetchPlaylistItems"))

      val futureResult: Future[List[Item]] =
        client invokePrivate fetchPlaylistItems(fourTrackFridayPlaylistId)
      val items = Await.result(futureResult, 10 second)
      items must have length 405
      items.head.added_at mustBe "2019-07-14T20:36:50Z"
    }
    "successfully get playlist tracks" in withRealClient { client =>
      val fetchTracks = PrivateMethod[Future[Map[String, Track]]](Symbol("fetchTracks"))

      val futureResult: Future[Map[String, Track]] =
        client invokePrivate fetchTracks(fourTrackFridayPlaylistId)
      val tracks = Await.result(futureResult, 10 second)
      tracks must have size 405
      tracks must contain key "1PR1JQmuOmI3eD4isHeLlI"
    }
    "successfully get feature metrics" in withRealClient { client =>
      val buildStats = PrivateMethod[Future[Map[String, Stat]]](Symbol("buildStats"))

      val futureResult: Future[Map[String, Stat]] =
        client invokePrivate buildStats(fourTrackFridayPlaylistId)
      val metrics = Await.result(futureResult, 10 second)
      metrics must have size 9
      metrics must contain key "tempo"
      metrics("tempo").minTrack.get.id mustBe "18uI37pTOz9tfk3U4jB8ci"
    }
    "successfully produce json of metrics" in withRealClient { client =>
      val futureResult: Future[String] = client.fetchStatistics(fourTrackFridayPlaylistId)
      val json                         = Await.result(futureResult, 10 second)

      // for debugging
      println(prettyPrint(Json.parse(json)))

      json must include("{\"tempo\":{\"name\":\"tempo\",\"minValue\":49.725,\"minAudioFeature\"")
      json must include(
        "\"info\":{\"name\":\"Four Track Friday\",\"owner\":{\"display_name\":\"Tanner\"},\"external_urls\":{\"spotify\":\"https://open.spotify.com/playlist/720360kMd4LiSAVzyA8Ft4\"}},"
      )
      json mustNot include("null")
    // this is obviously a fragile test, but it's what i need to ensure happy path is working
    }
  }
}
