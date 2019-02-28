package scalahttp

import java.util
import java.util.{Arrays, HashMap, List, Map}

import com.pi4j.io.gpio.Pin
import http.{HTTPServer, RESTRequestManager}
import relay.RelayManager
import sensors.ADCChannel
import utils.PinUtil

class ScalaHttpRequestManager extends RESTRequestManager {
  private val httpVerbose = "true" == System.getProperty("http.verbose", "false")
  private var restImplementation:ScalaRESTImplementation = null

  private var httpRequestServer = null
  // Physical
  private var relayManager:RelayManager = null
  private var adcChannel:ADCChannel = null


  def onExit(): Unit = { // Cleanup
    if ("true" == System.getProperty("server.verbose", "false")) {
      println("Cleaning up - HttpRequestManager")
    }
    this.relayManager.shutdown
    this.adcChannel.close
  }

  private def buildRelayMap(strMap: String) = {
    val map = new util.HashMap[Integer, Pin]
    val array = strMap.split(",")
    util.Arrays.stream(array).forEach((relayPrm: String) => {
      def foo(relayPrm: String) = {
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
          map.put(relayNum, physicalNumber)
        } catch {
          case nfe: NumberFormatException =>
            throw new RuntimeException(s"In [$strMap], element [$relayPrm], bad numbers")
        }
      }

      foo(relayPrm)
    })
    map
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

  /*
   Specific operations
   */

//  protected def getAllOperationList: util.List[HTTPServer.Operation] = httpRequestServer.getAllOperationList

}
