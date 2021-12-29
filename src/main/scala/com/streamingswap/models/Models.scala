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
