package scalahttp

import java.util

import com.pi4j.io.gpio.Pin
import http.{HTTPServer, RESTRequestManager}
import relay.RelayManager
import sensors.ADCChannel
import utils.PinUtil

class ScalaHttpRequestManager extends RESTRequestManager {
  private val httpVerbose = "true" == System.getProperty("http.verbose", "false")
  private var restImplementation:ScalaRESTImplementation = null

  private var httpRequestServer :ScalaHttpRequestServer.ScalaServer = null
  // Physical
  private var relayManager:RelayManager = null
  private var adcChannel:ADCChannel = null

  def this(parent: ScalaHttpRequestServer.ScalaServer) = {
    this()
    this.httpRequestServer = parent

    val mapStr = System.getProperty("relay.map", "1:11,2:12")
    //                                                        | |  | |
    //                                                        | |  | Physical pin #12 (GPIO_1)
    //                                                        | |  Relay num for this app
    //                                                        | Physical pin #11 (GPIO_0)
    //                                                        Relay num for this app
    var miso = 0
    var mosi = 10
    var clk = 11
    var cs = 8
    var channel = 0

    val misoStr = System.getProperty("miso.pin", String.valueOf(miso))
    val mosiStr = System.getProperty("mosi.pin", String.valueOf(mosi))
    val clkStr = System.getProperty("clk.pin", String.valueOf(clk))
    val csStr = System.getProperty("cs.pin", String.valueOf(cs))

    val adcChannelStr = System.getProperty("adc.channel", String.valueOf(channel))

    val relayMap = buildRelayMap(mapStr)

    try {
      miso = misoStr.toInt
      mosi = mosiStr.toInt
      clk = clkStr.toInt
      cs = csStr.toInt
      channel = adcChannelStr.toInt
    } catch {
      case nfe: NumberFormatException =>
        nfe.printStackTrace()
    }

    this.relayManager = new RelayManager(relayMap)
    this.adcChannel = new ADCChannel(miso, mosi, clk, cs, channel)

    this.restImplementation = new ScalaRESTImplementation(this)
    restImplementation.setRelayManager(this.relayManager)
    restImplementation.setADCChannel(this.adcChannel)
  }

  private def buildRelayMap(strMap: String): util.Map[Integer, Pin] = { // A Java Map is required later on.
    val pinMap = new util.HashMap[Integer, Pin]
    val array = strMap.split(",")
    array.foreach((relayPrm: String) => {
      val tuple = relayPrm.split(":")
      if (tuple == null || tuple.length != 2) {
        throw new RuntimeException(s"In [$strMap], bad element [$relayPrm]")
      }
      try {
        val relayNum = tuple(0).toInt
        val pinNum = tuple(1).toInt
        val physicalNumber = PinUtil.getPinByPhysicalNumber(pinNum)
        if (physicalNumber == null) {
          throw new RuntimeException(s"In [$strMap], element [$relayPrm], pin #$pinNum does not exist")
        }
        pinMap.put(relayNum, physicalNumber)
      } catch {
        case nfe: NumberFormatException =>
          throw new RuntimeException(s"In [$strMap], element [$relayPrm], bad numbers")
      }
    })
    pinMap
  }

  def onExit(): Unit = { // Cleanup
    if ("true" == System.getProperty("server.verbose", "false")) {
      println("Cleaning up - HttpRequestManager")
    }
    if (this.relayManager != null) {
      this.relayManager.shutdown
    }
    if (this.adcChannel != null) {
      this.adcChannel.close
    }
  }

  /**
    * Manage the REST requests.
    *
    * @param request incoming request
    * @return response as defined in the { @link RESTImplementation}
    * @throws UnsupportedOperationException
    */
  @throws[UnsupportedOperationException]
  override def onRequest(request: HTTPServer.Request): HTTPServer.Response = {
    val response = restImplementation.processRequest(request) // All the skill is here.
    if (this.httpVerbose) {
      println("======================================")
      println(s"Request :\n${request.toString}")
      println(s"Response :\n${response.toString}")
      println("======================================")
    }
    response
  }

  override def getRESTOperationList: util.List[HTTPServer.Operation] = restImplementation.getOperations
}
