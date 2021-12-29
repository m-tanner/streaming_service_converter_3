package com.streamingswap.clients

import com.streamingswap.{PlaySpec, Settings}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.ws.ahc._
import play.api.mvc._
import play.api.{Environment, Mode}

import java.io.File
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class SpotifyClientSpec extends PlaySpec with Results with GuiceOneAppPerSuite {

  private val clientId = "3613fbb9bb664052bd8d16d434cae265"
  private val clientSecret = "9df868eb39c5403692321df9ad7c5bfb"

  def withWs(testCode: AhcWSClient => Any): Any = {
    val environment = Environment(new File("."), this.getClass.getClassLoader, Mode.Test)
    val wsConfig = AhcWSClientConfigFactory.forConfig(classLoader = environment.classLoader)
    val mat = app.materializer
    val ws = AhcWSClient(wsConfig)(mat)
    try {
      testCode(ws) // loan the client
    } finally ws.close()

  }

  "SpotifyClient" must {
    "successfully retrieve a bearer token" in withWs { ws =>
      val controller = SpotifyClient(ws, Settings.apply)(global)
      val futureResult: Future[String] = controller.authorize(clientId, clientSecret)
      val result = Await.result(futureResult, 1 second)
      result must have length 83 // definitely a valid token
    }
  }
}
