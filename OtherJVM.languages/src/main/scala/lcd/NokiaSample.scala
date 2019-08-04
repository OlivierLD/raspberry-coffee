package lcd

import java.text.DecimalFormat

import spi.lcd.nokia.Nokia5110 // See this class for the wiring

object NokiaSample {
  var go = true
  val NF = new DecimalFormat("00.00")

  def main(args:Array[String]):Unit = {
    val lcd = new Nokia5110
    lcd.begin

    sys addShutdownHook {
      println("Shutdown hook caught.")
      lcd shutdown()
      go = false
      println("Bye.")
    }

    val sb = new ScreenBuffer(84, 48)
    while (go) {
      sb clear(ScreenBuffer.Mode.WHITE_ON_BLACK)
      sb text("BSP", 2, 9, ScreenBuffer.Mode.WHITE_ON_BLACK)
      val bsp = Math.random * 10.0
      val speed = NF.format(bsp)
      sb text(speed, 2, 19, 2, ScreenBuffer.Mode.WHITE_ON_BLACK)
      lcd setScreenBuffer(sb getScreenBuffer())
      lcd display()
    }
  }
}
