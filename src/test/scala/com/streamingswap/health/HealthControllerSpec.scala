package com.streamingswap.health

import com.streamingswap.{ PlaySpec, Settings }
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Future

class HealthControllerSpec extends PlaySpec with Results with GuiceOneAppPerSuite {

  private val settings = Settings.apply

  override def fakeApplication(): Application = {
    GuiceApplicationBuilder()
      .configure(
        Map(
          "app" -> Map(
            "config" -> Map(
              "apexCommonName" -> "apex"
            )
          )
        )
      )
      .build()
  }

  "HealthController" must {
    "return synchronous valid 200 response" in {
      val controller             = HealthController(Helpers.stubControllerComponents(), settings)
      val result: Future[Result] = controller.index().apply(FakeRequest())
      val bodyText: String       = contentAsString(result)
      bodyText mustBe "service is online"
    }
    "return asynchronous valid 200 response" in {
      val controller             = HealthController(Helpers.stubControllerComponents(), settings)
      val result: Future[Result] = controller.asyncIndex().apply(FakeRequest())
      val bodyText: String       = contentAsString(result)
      bodyText mustBe "service is online"
    }
  }
}
