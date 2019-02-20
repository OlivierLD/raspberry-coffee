package olivscala

/*
   1. Compile (from the scala directory)
   $ scalac olivscala.ShortestRoute.scala

   2. Run (from the same place)
   $ scala olivscala.ShortestRoute

 */

import java.io.FileReader
import java.util.Properties

object ShortestRoute {

  class Point {
    private var name: String = _
    private var x = 0
    private var y = 0

    def name(name: String): Point = {
      this.name = name
      this
    }

    def x(x: Int): Point = {
      this.x = x
      this
    }

    def y(y: Int): Point = {
      this.y = y
      this
    }

    def getName: String = {
      this.name
    }

    def dist(pt: Point): Double = Math.sqrt(Math.pow(pt.x - this.x, 2) + Math.pow(pt.y - this.y, 2))
  }

  class Route {
    private var name: String = _
    private var len = .0

    def name(name: String): Route = {
      this.name = name
      this
    }

    def len(len: Double): Route = {
      this.len = len
      this
    }

    def getLen: Double = this.len

    def getName: String = this.name
  }

  private val verbose = "true" == System.getProperty("verbose")

  @throws[Exception]
  private def generatePoints(propFile: String): List[Point] = {
    var list: List[Point] = List()
    val props = new Properties
    props.load(new FileReader(propFile))
    var pt = 1
    var go = true
    while (go) {
      val name = props.getProperty(s"p$pt.name")
      if (name != null) {
        val x = props.getProperty(s"p$pt.x").toInt
        val y = props.getProperty(s"p$pt.y").toInt
        val p = new Point().name(name).x(x).y(y)
        list = list :+ p
        pt += 1
      } else {
        go = false
      }
    }
    list
  }

  @throws[Exception]
  def main(args: Array[String]): Unit = {
    var pointList: List[Point] = null
    val propertyFile = System.getProperty("props")
    if (propertyFile == null) { // Hard coded default point list
      val A = new Point().x(0).y(0).name("A")
      val B = new Point().x(300).y(0).name("B")
      val C = new Point().x(300).y(500).name("C")
      val D = new Point().x(0).y(500).name("D")
      pointList = List(A, B, C, D)
    } else {
      pointList = generatePoints(propertyFile)
    }
    var routeChoices = List[Route]()
    var startFrom = 0
    while (startFrom < pointList.length) {
      val startPoint = pointList(startFrom)
      if (verbose) {
        println(s"Starting from ${startPoint.getName}")
      }
      var path = List[Point](startPoint)
      val toEvaluate = pointList.filter(_ != startPoint) // All points except the one you start from
      // Collectors.toList
      var from = startPoint
      var i = 0
      while (i < toEvaluate.size) {
        var closestPointIndex = -1
        var smallestDist = Double.MaxValue
        var prog = 0
        while (prog < toEvaluate.size) {
          if (!from.equals(toEvaluate(prog))) {
            val dist = from.dist(toEvaluate(prog))
            if (dist < smallestDist && !path.contains(toEvaluate(prog))) {
              closestPointIndex = prog
              smallestDist = dist
            }
          }
          prog += 1
        }
        if (closestPointIndex != -1) {
          if (verbose) {
            println(s"\tClosest from ${from.getName} is ${toEvaluate(closestPointIndex).getName} ($smallestDist)")
          }
          path = path :+ toEvaluate(closestPointIndex)
          from = toEvaluate(closestPointIndex)
        }
        i += 1
      }

      val pathStr = path.map(_.getName).mkString(",")
      var lastPoint: Point = null
      var routeLen: Double = 0
      for (pt <- path) {
        if (lastPoint != null) {
          routeLen += lastPoint.dist(pt)
        }
        lastPoint = pt
      }
      if (verbose) {
        println(s"Result: For path $pathStr, length is $routeLen ")
      }
      routeChoices = routeChoices :+ new Route().name(pathStr).len(routeLen)
      startFrom += 1
    }
    // Find shortest route
    val bestRoute = routeChoices.min((r1: Route, r2: Route) => r1.getLen compare r2.getLen)
    println(s">> Shortest route is ${bestRoute.getName}, ${bestRoute.getLen}")
  }
}
