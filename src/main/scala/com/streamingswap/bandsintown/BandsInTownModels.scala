package com.streamingswap.bandsintown

case class Venue(name: String, city: String, region: String, country: String)
case class Event(id: String, artist_id: String, title: String, datetime: String, venue: Venue)
case class Events(events: List[Event])
