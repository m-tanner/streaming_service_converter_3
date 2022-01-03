package com.streamingswap.spotify

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
) {
  def extract(field: String): Float = {
    field match {
      case "acousticness"     => acousticness
      case "danceability"     => danceability
      case "duration_ms"      => duration_ms.toFloat
      case "energy"           => energy
      case "instrumentalness" => instrumentalness
      case "liveness"         => liveness
      case "loudness"         => loudness
      case "tempo"            => tempo
      case "valence"          => valence
    }
  }
}
case class AudioFeatures(audio_features: List[AudioFeature])
case class Stat(
  name: String,
  minValue: Float = Float.MaxValue,
  minAudioFeature: Option[AudioFeature] = None,
  minTrack: Option[Track] = None,
  maxValue: Float = Float.MinValue,
  maxAudioFeature: Option[AudioFeature] = None,
  maxTrack: Option[Track] = None,
  sum: Float = 0,
  avg: Float = Float.NaN,
)
