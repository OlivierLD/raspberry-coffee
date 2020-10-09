package olivscala

import scala.collection.mutable
import scala.util.Random

object LowPassFilter {

  private val ALPHA = 0.15f // For the low pass filter (smoothing)
  private var accumulator:Double = 0

  private def lowPass(alpha: Double, value: Double, acc: Double) = (value * alpha) + (acc * (1d - alpha))

  var doubleData = new mutable.MutableList[Double]()

  def main(args: Array[String]): Unit = {

    val random = Random
    var x = 0
    for (x  <- 1 to 1000) {
      doubleData += (100 * random.nextDouble());
    }

    doubleData.foreach(d => accumulator = lowPass(ALPHA, d, accumulator))
    println(s"Filtered: $accumulator")
  }

}
