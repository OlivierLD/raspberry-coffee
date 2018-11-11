import processing.core._
import processing.core.PConstants._
import scala.util.Random

object Plasma extends PApplet {

  val SCREENWIDTH = 640
  val SCREENHEIGHT = 360
  val GRADIENTLEN = 1500
  val SPEEDUP = 3
  val SWINGLEN = GRADIENTLEN * 3
  val SWINGMAX = GRADIENTLEN / 2 - 1

  private var plasma:Plasma = _

  def main(args: Array[String]) = {
    import processing.core.PApplet
    val appletArgs = Array[String]("Plasma")
    if (args != null) {
      PApplet.main(appletArgs ++ args)
    } else {
      PApplet.main(appletArgs)
    }
  }
}

class Plasma extends PApplet {

  import Plasma._

  var colorGrad:Array[Int] = _
  var swingCurve:Array[Int] = _

  override def settings() {
//  size(640, 360, "processing.opengl.PGraphics2D");
    size( SCREENWIDTH, SCREENHEIGHT, P2D )
  }

  override def setup() = {
//  size( SCREENWIDTH, SCREENHEIGHT, P2D )
    makeGradient( GRADIENTLEN )
    makeSwingCurve( SWINGLEN, SWINGMAX )
  }

  override def draw() = {
    loadPixels
    var i = 0
    val t = frameCount * SPEEDUP
    val swingT = swing(t)

    for ( y <- 0 to SCREENHEIGHT-1 ) {
      val swingY  = swing(y)
      val swingYT = swing(y + t)
      for ( x <- 0 to SCREENWIDTH-1 ) {
        pixels(i) = gradient(
          swing(swing(x + swingT) + swingYT) +
            swing(swing(x + t     ) + swingY ))
        i += 1
      }
    }
    updatePixels
  }

  override def mousePressed = {
    if ( mouseButton == LEFT ) {
      makeGradient( GRADIENTLEN )
    } else if ( mouseButton == RIGHT ) {
      makeSwingCurve( SWINGLEN, SWINGMAX )
    }
  }

  def makeSwingCurve(arrlen:Int, maxval:Int) = {
    var factor1 = 2
    var factor2 = 3
    var factor3 = 6

    if ( swingCurve == null ) {
      swingCurve = new Array[Int]( SWINGLEN )
    } else {
      factor1 = Random.nextInt(6) + 1
      factor2 = Random.nextInt(6) + 1
      factor3 = Random.nextInt(6) + 1
    }

    val halfmax = (maxval/factor1).toInt

    for( i <- 0 to arrlen-1 ) {
      val ni = i * TWO_PI / arrlen
      swingCurve(i) = (math.cos( ni*factor1 ) * math.cos( ni*factor2 ) * math.cos( ni*factor3 ) * halfmax + halfmax ).toInt
    }
  }

  def makeGradient(arrlen:Int) = {
    var rf = 4
    var gf = 2
    var bf = 1
    var rd = 0
    var gd = arrlen / gf;
    var bd = arrlen / bf / 2

    if ( colorGrad == null ) {
      colorGrad = new Array[Int](GRADIENTLEN)
    } else {
      rf = Random.nextInt(6) + 1
      gf = Random.nextInt(6) + 1
      bf = Random.nextInt(6) + 1
      rd = Random.nextInt(arrlen)
      gd = Random.nextInt(arrlen)
      bd = Random.nextInt(arrlen)
      println("Gradient factors("+rf+","+gf+","+bf+"), displacement("+rd+","+gd+","+bd+")")
    }

    for ( i <- 0 to arrlen-1 ) {
      val r = cos256(arrlen / rf, i + rd)
      val g = cos256(arrlen / gf, i + gd)
      val b = cos256(arrlen / bf, i + bd)
      colorGrad(i) = color(r, g, b)
    }
  }

  def cos256(amplitude:Int, x:Int) = {
    (math.cos(x * TWO_PI / amplitude) * 127).toInt + 127
  }

  def swing(i:Int) = {
    swingCurve(i % SWINGLEN)
  }

  def gradient(i:Int) = {
    colorGrad(i % GRADIENTLEN)
  }

}
