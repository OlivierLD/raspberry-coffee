package smoothing

import java.io.{BufferedReader, FileReader, IOException}

import systems.{SquareMatrix, SystemUtils}

/**
  * For details on the least squares method:
  * See http://www.efunda.com/math/leastsquares/leastsquares.cfm
  * See http://www.lediouris.net/original/sailing/PolarCO2/index.html
  */
object LeastSquares {

  private def f(x: Double, coeffs: Double*) = {
    var result = 0.0
    for (deg:Int <- 0 until coeffs.length) {
      result += (coeffs(deg) * Math.pow(x, coeffs.length - (deg + 1)))
    }
    result
  }

  def main(args:Array[String]): Unit = {
    // Read the data generated in Java (see raspisamples.smoothing.LeastSquaresMethod.java)
    var data = List.empty[(Double, Double)]
    try {
      var br = new BufferedReader(new FileReader("cloud.csv"))
      var line = ""
      var go = true
      while (go) {
        line = br.readLine()
        if (line == null) {
          go = false
        } else {
          var tuple = line split ";"
          data :+= (tuple(0).toDouble, tuple(1).toDouble)
        }
      }
      br.close()
    } catch {
      case ioe: IOException =>
        ioe.printStackTrace()
    }

    if (!data.isEmpty) { // Then we're good
      println("Data read OK")
      val requiredDegree = 2
      val dimension = requiredDegree + 1

      var sumXArray = Array.ofDim[Double]((requiredDegree * 2) + 1)
      var sumY      = Array.ofDim[Double](requiredDegree + 1)
      // Init
      for (idx <- 0 until (requiredDegree * 2) + 1) {
        sumXArray(idx) = 0.0
      }
      for (idx <- 0 until requiredDegree + 1) {
        sumY(idx) = 0.0
      }
      // Now, process
      data.foreach(tuple => {
        for (idx <- 0 until (requiredDegree * 2) + 1) {
          sumXArray(idx) += Math.pow(tuple._1, idx)
        }
        for (idx <- 0 until requiredDegree + 1) {
          sumY(idx) += (tuple._2 * Math.pow(tuple._1, idx))
        }
      })

      var squareMatrix = new SquareMatrix(dimension)
      for (row <- 0 until dimension) {
        for (col <- 0 until dimension) {
          var powerRnk = (requiredDegree - row) + (requiredDegree - col)
          squareMatrix setElementAt(sumXArray(powerRnk), row, col)
        }
      }
      var constants = Array.ofDim[Double](dimension)
      for (idx <- 0 until dimension) {
        constants(idx) = sumY(requiredDegree - idx)
      }

      println("Resolving:")
      SystemUtils printSystem(squareMatrix, constants)
      println("")

      var result = SystemUtils solveSystem(squareMatrix, constants)
      result.foreach(coef =>
        println(s"${coef}")
      )

    } else {
      println("No data")
    }
  }
}
