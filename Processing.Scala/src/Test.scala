import processing.core._

import math._
import scala.util.Random

/**
  * Adapted from http://va.lent.in/scala-processing/
  */
object Test extends PApplet {

  private var test:Test = _

  def main(args: Array[String]) = {

    import processing.core.PApplet
    val appletArgs = Array[String]("Test")
    if (args != null) {
      PApplet.main(appletArgs ++ args)
    } else {
      PApplet.main(appletArgs)
    }
  }
}

class Test extends PApplet {

  var angle:Int = 0

  override def settings() {
    size(640, 360)
  }

  override def setup() = {
//    size(640, 360)
    background(102)
    smooth()
    noStroke()
    fill(0, 102)
  }

  override def draw() = {
    angle += 10
    var value = cos(toRadians(angle)) * 6.0
    for (a <- 0 to 360 by 75) {
      val xoff = cos(toRadians(a)) * value
      val yoff = sin(toRadians(a)) * value
      fill(Random.nextInt(255))
      ellipse( (mouseX + xoff).toFloat, (mouseY + yoff).toFloat, value.toFloat, value.toFloat );
    }
    fill(255);
    ellipse(mouseX, mouseY, 2, 2)
  }
}
