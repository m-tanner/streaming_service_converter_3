package com.streamingswap

import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class SettingsSpec extends PlaySpec with GuiceOneAppPerSuite {

  private val settings = Settings.apply

  "Settings" must {
    "contain the proper settings" in {
      settings.rootUrl mustBe "https://streamingswap.com"
      settings.spotifyApiUrl mustBe "https://api.spotify.com"
      settings.spotifyAuthUrl mustBe "https://accounts.spotify.com"
      settings.spotifyClientId mustBe "3613fbb9bb664052bd8d16d434cae265"
      settings.spotifyClientSecret must have length 32
    }
    "be able to load from application configuration explicitly" in {
      val rootUrl = app.configuration.get[String]("app.config.rootUrl")
      rootUrl mustEqual "https://streamingswap.com"
    }
  }
}
