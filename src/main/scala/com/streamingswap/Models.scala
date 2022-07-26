package com.streamingswap

import java.util.regex.Pattern

case class PlaylistId(id: String) {
  override def toString: String = id

  def hasNonAlphanumeric: Boolean = {
    val pattern = Pattern.compile("[^a-zA-Z0-9]")
    pattern.matcher(id).find()
  }
}
case class ThingToMakeIdeShutUp()
