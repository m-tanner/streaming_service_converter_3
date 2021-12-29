package com.streamingswap

import com.typesafe.config.{Config, ConfigFactory}

import javax.inject.Inject

class Settings @Inject()(config: Config) {
  val rootUrl: String = config.getString("app.config.rootUrl")
  val spotifyAuthUrl: String = config.getString("app.config.spotify.authUrl")
  val spotifyApiUrl: String = config.getString("app.config.spotify.apiUrl")
}

object Settings {
  def apply: Settings = {
    new Settings(ConfigFactory.load())
  }
}
