package scalahttp

import http.HTTPServer

object ScalaHttpRequestServer {

  def main(args: Array[String]): Unit = {
    println("Very minimal...")
    System.setProperty("http.verbose", "true")

    val httpServer = new HTTPServer(1234)

    sys addShutdownHook {
      println("\nUser interrupted.")
      httpServer.stopRunning
      println("Bye.")
    }

    httpServer.startServer
  }

}
