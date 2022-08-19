package scalahttp

import java.io.StringReader
import java.util

import com.google.gson.{Gson, GsonBuilder}
import http.HTTPServer.Response
import http.{HTTPServer, RESTProcessorUtil}
import relay.RelayManager
import sensors.ADCChannel

class ScalaRESTImplementation {
  private val verbose = "true" == System.getProperty("server.verbose", "false")
  private val SERVER_PREFIX = "/server"
  private val RELAY_PREFIX = "/relay"
  private val LIGHT_PREFIX = "/light"

  private var httpRequestManager: ScalaHttpRequestManager = null
  private var physicalRelayManager: RelayManager = null
  private var physicalADCChannel: ADCChannel = null

  def this(restRequestManager: ScalaHttpRequestManager) = {
    this()
    this.httpRequestManager = restRequestManager
    // Check duplicates in operation list. Barfs if duplicate is found.
    RESTProcessorUtil.checkDuplicateOperations(operations)
  }

  def setRelayManager(relayManager: RelayManager): Unit = {
    this.physicalRelayManager = relayManager
  }

  def setADCChannel(adcChannel: ADCChannel): Unit = {
    this.physicalADCChannel = adcChannel
  }

  /**
    * Define all the REST operations to be managed
    * by the HTTP server.
    * <p>
    * Frame path parameters with curly braces.
    * <p>
    * See {@link #processRequest(Request)}
    * See {@link HTTPServer}
    */
  private val operations = util.Arrays.asList(
    new HTTPServer.Operation(
      "GET",
      SERVER_PREFIX + "/oplist",
      this.getOperationList,
      "List of all available operations on the Relay service."
    ),
    new HTTPServer.Operation(
      "POST",
      RELAY_PREFIX + "/status/{relay-id}",
      this.setRelayStatus,
      "Set the repay status, and return its json representation."),
    new HTTPServer.Operation(
      "GET",
      RELAY_PREFIX + "/status/{relay-id}",
      this.getRelayStatus,
      "Get the relay status"),
    new HTTPServer.Operation(
      "GET",
      LIGHT_PREFIX + "/ambient",
      this.getAmbientLight,
      "Get the ambient light in %")
  )

  def getOperations: util.List[HTTPServer.Operation] = this.operations

  /**
    * This is the method to invoke to have a REST request processed as defined above.
    *
    * @param request as it comes from the client
    * @return the actual result.
    */
  @throws[UnsupportedOperationException]
  def processRequest(request: HTTPServer.Request): HTTPServer.Response = {
    val opOp = operations.stream.filter((op: HTTPServer.Operation) => op.getVerb == request.getVerb && RESTProcessorUtil.pathMatches(op.getPath, request.getPath)).findFirst
    if (opOp.isPresent) {
      val op = opOp.get
      request.setRequestPattern(op.getPath) // To get the prms later on.

      val processed = op.getFn.apply(request) // Execute here.
      processed
    }
    else throw new UnsupportedOperationException(String.format("%s not managed", request.toString))
  }

  private def getOperationList(request: HTTPServer.Request) = {
    val response = new HTTPServer.Response(request.getProtocol, Response.STATUS_OK)
    val opList = this.getOperations
    val content = new Gson().toJson(opList)
    RESTProcessorUtil.generateResponseHeaders(response, content.length)
    response.setPayload(content.getBytes)
    response
  }

  /**
    * The payload is a requests like this
    *
    * { "status": false }
    *
    * @param request
    * @return
    */
  private def setRelayStatus(request: HTTPServer.Request): HTTPServer.Response = {
    var response = new HTTPServer.Response(request.getProtocol, Response.STATUS_OK)
    val pathParameters = request.getPathParameters
    if (verbose) {
      val pathPrmNames = request.getPathParameterNames
      for (i <- 0 to (pathPrmNames.size - 1)) {
        println(s"${pathPrmNames.get(i)} = ${pathParameters.get(i)}")
      }
    }
    if (request.getContent != null && request.getContent.length > 0) {
      val payload = new String(request.getContent)
      if (!("null" == payload)) {
        if (verbose) {
          println(s"Tx Request: $payload")
        }
        val gson = new GsonBuilder().create
        val stringReader = new StringReader(payload)
        try {
          val relayStatus = gson.fromJson(stringReader, classOf[RelayStatus])
          val relayNum = pathParameters.get(0).toInt
          // Set Relay status here
          if (this.physicalRelayManager != null) {
            this.physicalRelayManager.set(relayNum, if (relayStatus.status) "on" else "off")
          }
          val content = new Gson().toJson(relayStatus)
          RESTProcessorUtil.generateResponseHeaders(response, content.length)
          response.setPayload(content.getBytes)
        } catch {
          case ex1: Exception =>
            ex1.printStackTrace
            response = HTTPServer.buildErrorResponse(response, Response.BAD_REQUEST, new HTTPServer.ErrorPayload().errorCode("RELAY-0003").errorMessage(ex1.toString))
            return response
        }
      } else {
        response = HTTPServer.buildErrorResponse(response, Response.BAD_REQUEST, new HTTPServer.ErrorPayload().errorCode("RELAY-0002").errorMessage("Request payload not found"))
        return response
      }
    } else {
      response = HTTPServer.buildErrorResponse(response, Response.BAD_REQUEST, new HTTPServer.ErrorPayload().errorCode("RELAY-0002").errorMessage("Request payload not found"))
      return response
    }
    response
  }

  /**
    * @param request
    * @return
    */
  private def getRelayStatus(request: HTTPServer.Request) = {
    var response = new HTTPServer.Response(request.getProtocol, Response.STATUS_OK)
    val pathParameters = request.getPathParameters
    val rs = new RelayStatus
    // Get status here
    try {
      val relayNum = pathParameters.get(0).toInt
      val onOff = this.physicalRelayManager.get(relayNum)
      rs.status = onOff
      val content = new Gson().toJson(rs)
      RESTProcessorUtil.generateResponseHeaders(response, content.length)
      response.setPayload(content.getBytes)
      response
    } catch {
      case ex1: Exception =>
        ex1.printStackTrace
        response = HTTPServer.buildErrorResponse(response, Response.BAD_REQUEST, new HTTPServer.ErrorPayload().errorCode("RELAY-0004").errorMessage(ex1.toString))
        response
    }
  }

  private def getAmbientLight(request: HTTPServer.Request) = {
    var response = new HTTPServer.Response(request.getProtocol, Response.STATUS_OK)
    val al = new AmbientLight
    try {
      val ambient = this.physicalADCChannel.readChannelVolume
      al.percent = ambient
      val content = new Gson().toJson(al)
      RESTProcessorUtil.generateResponseHeaders(response, content.length)
      response.setPayload(content.getBytes)
      response
    } catch {
      case ex1: Exception =>
        ex1.printStackTrace
        response = HTTPServer.buildErrorResponse(response, Response.BAD_REQUEST, new HTTPServer.ErrorPayload().errorCode("LIGHT-0001").errorMessage(ex1.toString))
        response
    }
  }

  /**
    * Can be used as a temporary placeholder when creating a new operation.
    *
    * @param request
    * @return
    */
  private def emptyOperation(request: HTTPServer.Request) = {
    val response = new HTTPServer.Response(request.getProtocol, Response.NOT_IMPLEMENTED)
    response
  }

  class RelayStatus {
    var status = false
  }

  class AmbientLight {
    var percent = .0
  }

}
