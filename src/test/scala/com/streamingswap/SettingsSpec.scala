package com.streamingswap

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

class SettingsSpec extends PlaySpec with GuiceOneAppPerSuite {

  private val settings = Settings.apply

  override def fakeApplication(): Application = {
    GuiceApplicationBuilder()
      .configure(
        Map(
          "app" -> Map(
            "config" -> Map(
              "rootUrl" -> "https://streamingswap.com"
            )
          )
        )
      )
      .build()
  }

  "Settings" must {
    "contain the test parameters" in {
      settings.rootUrl mustBe "https://streamingswap.com"
    }
    "be loaded from application configuration explicitly " in {
      val rootUrl = app.configuration.get[String]("app.config.rootUrl")
      rootUrl mustEqual "https://streamingswap.com"
    }
  }
}
