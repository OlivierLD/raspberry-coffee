package sensor

import java.io.IOException

import com.pi4j.io.i2c.I2CFactory
import i2c.sensor.VL53L0X

object VL53L0XTest {
  def main(args: String *): Unit = {
    try {
      val vl53l0x = new VL53L0X
      var previousDist = -1
      while (true) {
        val mm = vl53l0x.range
        if (previousDist != mm) println(s"Range: ${mm} mm")
        previousDist = mm
        try
          Thread sleep(50L)
        catch {
          case iex: InterruptedException =>
            iex printStackTrace
        }
      }
    } catch {
      case ioex: IOException =>
        ioex printStackTrace
      case ubne: I2CFactory.UnsupportedBusNumberException =>
        ubne printStackTrace
    }
  }

}
