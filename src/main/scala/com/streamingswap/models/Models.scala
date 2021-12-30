package com.streamingswap.models

case class ClientCredentials(access_token: String, token_type: String, expires_in: Int)
case class Playlist(name: String, owner: Map[String, String], external_urls: Map[String, String])
case class Artist(id: String, name: String)
case class Image(height: Int, url: String, width: Int)
case class Album(album_type: String, images: List[Image], name: String, release_date: String)
case class Track(
  album: Album,
  artists: List[Artist],
  duration_ms: Int,
  explicit: Boolean,
  external_urls: Map[String, String],
  id: String,
  name: String,
  popularity: Int,
)
case class Item(added_at: String, track: Track)
case class PlaylistTracks(href: String, items: List[Item], limit: Int, next: Option[String], offset: Int, total: Int)
case class AudioFeature(
  acousticness: Float,
  danceability: Float,
  duration_ms: Int,
  energy: Float,
  id: String,
  instrumentalness: Float,
  key: Int,
  liveness: Float,
  loudness: Float,
  mode: Int,
  speechiness: Float,
  tempo: Float,
  time_signature: Int,
  valence: Float,
)
case class AudioFeatures(audio_features: List[AudioFeature])
case class Stat(name: String, min: AudioFeature, max: AudioFeature, sum: Float = 0, avg: Float = Float.NaN)
