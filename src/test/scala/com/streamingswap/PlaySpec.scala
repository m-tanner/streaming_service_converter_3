package com.streamingswap

import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.WsScalaTestClient

/**
 * Shim copied from
 * https://github.com/playframework/scalatestplus-play/blob/master/module/src/main/scala/org/scalatestplus/play/PlaySpec.scala.
 *
 * As of 2/20/20, PlaySpec from scalatestplus-play is incompatible with scalatest 3.1.0.
 *
 * scalatestplus-play has not yet released a new version using ScalaTest 3.1.X, hence the need for a shim. Otherwise,
 * using scalatestplus-play will transitively pull in an outdated version of org/scalatest/Matchers.
 */
abstract class PlaySpec extends AnyWordSpec with Matchers with OptionValues with WsScalaTestClient
