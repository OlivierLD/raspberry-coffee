package olivscala

import scala.collection.mutable
import scala.util.Random

object LowPassFilter {

  private val ALPHA = 0.15f // For the low pass filter (smoothing)
  private var accumulator:Double = _

  private def lowPass(alpha: Double, value: Double, acc: Double) = (value * alpha) + (acc * (1d - alpha))

  var doubleData = new mutable.ListBuffer[Double]()

  def main(args: Array[String]): Unit = {

    val random = Random
    var x = 0
    for (x  <- 1 to 1000) {
      doubleData += (100 * random.nextDouble());
    }
    println(s"Raw Data: $doubleData")

    var filteredData = new mutable.ListBuffer[Double]()
    doubleData.foreach(d => {
      accumulator = lowPass(ALPHA, d, accumulator)
      filteredData+= accumulator
    })
    println(s"Filtered: $filteredData")
  }

}
