package com.streamingswap

import com.typesafe.config.{ Config, ConfigFactory }

import javax.inject.Inject

class Settings @Inject() (config: Config) {
  val rootUrl: String             = config.getString("app.config.rootUrl")
  val spotifyAuthUrl: String      = config.getString("app.config.spotify.authUrl")
  val spotifyApiUrl: String       = config.getString("app.config.spotify.apiUrl")
  val spotifyClientId: String     = config.getString("app.config.spotify.clientId")
  val spotifyClientSecret: String = config.getString("app.config.spotify.clientSecret")
  require(spotifyClientSecret != "changeme", "spotify client secret not set as an environment variable")
  val bandsInTownUrl: String = config.getString("app.config.bandsInTown.apiUrl")
  val bandsInTownAppId: String = config.getString("app.config.bandsInTown.appId")
  require(bandsInTownAppId != "changeme", "bandsintown app id not set as an environment variable")
  val ticketmasterClientId: String     = config.getString("app.config.ticketmaster.clientId")
  val ticketmasterClientSecret: String = config.getString("app.config.ticketmaster.clientSecret")
  require(spotifyClientSecret != "changeme", "ticketmaster client secret not set as an environment variable")
}

object Settings {
  def apply: Settings = {
    new Settings(ConfigFactory.load())
  }
}
