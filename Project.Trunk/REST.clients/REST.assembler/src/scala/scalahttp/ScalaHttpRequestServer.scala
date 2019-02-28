package scalahttp

import java.util.stream.Collectors

import http.{HTTPServer, RESTRequestManager}
import httpserver.HttpRequestManager

object ScalaHttpRequestServer {

  class ScalaServer {
    var httpServer: HTTPServer = null
    var httpPort = 9999

    def ScalaServer() {
      val port = System.getProperty("http.port")
      if (port != null) {
        httpPort = port.toInt
      }
      httpServer = startHttpServer(httpPort, new ScalaHttpRequestManager().asInstanceOf[HttpRequestManager])
    }

    def startHttpServer(port: Int, requestManager: HttpRequestManager): HTTPServer = {
      var newHttpServer:HTTPServer = null
      try {
        newHttpServer = new HTTPServer(port, requestManager) {
          override def onExit(): Unit = {
            requestManager.onExit()
          }
        }
        newHttpServer.startServer()
        //		newHttpServer.stopRunning();
      } catch {
        case e: Exception =>
          e.printStackTrace()
      }
      newHttpServer
    }

//  def getAllOperationList: java.util.List[HTTPServer.Operation] = this.httpServer.getRequestManagers.stream.flatMap((requestManager: RESTRequestManager) => requestManager.getRESTOperationList.stream).collect(Collectors.toList)

  }

  def main(args: Array[String]): Unit = {
    System.setProperty("http.verbose", "true")
    val httpServer = new ScalaServer
  }

}
