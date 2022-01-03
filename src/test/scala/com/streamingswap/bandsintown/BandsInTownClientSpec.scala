package com.streamingswap.bandsintown

import com.streamingswap.{ PlaySpec, Settings }
import org.scalatest.PrivateMethodTester
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.ws.ahc._
import play.api.mvc._
import play.api.{ Environment, Mode }

import java.io.File
import scala.concurrent.ExecutionContext.global
import scala.concurrent.ExecutionContextExecutor
import scala.language.postfixOps

class BandsInTownClientSpec extends PlaySpec with Results with PrivateMethodTester with GuiceOneAppPerSuite {

  private val settings                      = Settings.apply
  implicit val ec: ExecutionContextExecutor = global

  def withRealClient(testCode: BandsInTownClient => Any): Any = {
    val environment = Environment(new File("."), this.getClass.getClassLoader, Mode.Test)
    val wsConfig    = AhcWSClientConfigFactory.forConfig(classLoader = environment.classLoader)
    val mat         = app.materializer
    val ws          = AhcWSClient(wsConfig)(mat)
    try {
      testCode(BandsInTownClient(ws, settings)(ec)) // loan the client
    } finally ws.close()

  }

  "TicketmasterClient" must {
    "successfully do nothing" in withRealClient(client => println("pass"))
  }
}
