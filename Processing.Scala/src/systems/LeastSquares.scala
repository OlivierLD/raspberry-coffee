package systems

import processing.core._
import java.util

object LeastSquares extends PApplet {

  private var leastSquares:LeastSquares = _

  def main(args: Array[String]) = {

    import processing.core.PApplet
    val appletArgs = Array[String]("systems.LeastSquares")
    if (args != null) {
      PApplet.main(appletArgs ++ args)
    } else {
      PApplet.main(appletArgs)
    }
  }
}

class LeastSquares extends PApplet {

  var requiredSmoothingDegree = 3

  class Point(var _x: Int, var _y: Int) {
    val x = _x
    val y = _y
  }

  var hsbDegree: HScrollBar = null

  var points = new util.ArrayList[Point]
  var coeffs: Array[Double] = null

  val WHITE = 255
  val BLACK = 0
  val BLUE: Int = color(0, 0, 255)
  val RED: Int = color(255, 0, 0)

  val BUTTON_COLOR: Int = BLACK
  val BUTTON_HIGHLIGHT: Int = color(128)
  val BUTTON_CLICKED: Int = color(200)

  val BUTTON_RESOLVE_LABEL = "Resolve"
  val BUTTON_CLEAR_LABEL = "Clear"

  var buttonResolveColor: Int = BUTTON_COLOR
  var buttonClearColor: Int = BUTTON_COLOR

  val buttonFontSize = 14
  val buttonTextPadding = 3
  val buttonMargin = 5

  val buttonResolvePosX = 10
  val buttonResolvePosY = 10
  val buttonResolveHeight: Int = buttonFontSize + (2 * buttonTextPadding)
  var buttonResolveWidth = 0

  var buttonClearPosX = 0

  var buttonClearWidth = 0
  var buttonClearPosY = 10
  var buttonClearHeight: Int = buttonFontSize + (2 * buttonTextPadding)

  var buttonResolveOver = false
  var buttonClearOver = false

  val SLIDER_PADDING = 10
  val CURSOR_SIZE = 16

  override def settings() {
    size(640, 640)
  }

  override def setup() = {
    hsbDegree = new HScrollBar(SLIDER_PADDING, height - 10, width - (2 * SLIDER_PADDING), CURSOR_SIZE, CURSOR_SIZE)
    hsbDegree.setPos(degToSliderPos(requiredSmoothingDegree))
  }

  def sliderToDegValue: Int = {
    var degree = hsbDegree.getPos
    degree -= SLIDER_PADDING
    val sliderWidth = width - (2 * SLIDER_PADDING)
    degree /= sliderWidth
    // from 1 to 8
    val intDeg = (1 + degree * 7.round.toInt).asInstanceOf[Int]
    //println(String.format("From slider: %f", degree));
    intDeg
  }

  def degToSliderPos(d: Int): Float = {
    val sliderWidth = width - (2 * SLIDER_PADDING)
    val pos = ((d - 1).toFloat / 7f) * sliderWidth.toFloat
    //println(String.format("Deg: %d, SPos: %f", d, pos));
    pos
  }

  var prevDegree = 0

  override def draw() = {
    background(WHITE)
    fill(BLACK)

    requiredSmoothingDegree = sliderToDegValue
    text("Drag the mouse to spray points, then click [Resolve]. Degree is " + String.valueOf(requiredSmoothingDegree), 10, height - 50)
    text("Use the slider to change the degree of the curve to calculate", 10, height - 30)

    hsbDegree.update
    hsbDegree.display

    if ((prevDegree != requiredSmoothingDegree) && points.size > 2) { // Then recalculate
      smoothing
    }
    prevDegree = requiredSmoothingDegree

    // Points
    if (points.size > 0) {
      stroke(BLUE)
      points.forEach(pt => {
        point(pt.x, pt.y)
      })
    }
    // Curve?
    if (coeffs != null) {
      stroke(RED)
      var prevPt:Point = null
      for (x <- 0 until width) {
        val y = func(x, coeffs).asInstanceOf[Int]
        if (prevPt != null) {
          line(prevPt.x, prevPt.y, x, y)
        }
        prevPt = new Point(x, y)
      }
    }

    // Button states
    update(mouseX, mouseY)

    // Resolve button
    noStroke
    fill(buttonResolveColor)
    buttonResolveWidth = (textWidth(BUTTON_RESOLVE_LABEL) + (2 * buttonTextPadding)).asInstanceOf[Int]
    rect(buttonResolvePosX, buttonResolvePosY, buttonResolveWidth, buttonResolveHeight)
    textSize(buttonFontSize)
    fill(WHITE)
    text(BUTTON_RESOLVE_LABEL, buttonResolvePosX + buttonTextPadding, buttonResolvePosY + buttonFontSize + buttonTextPadding)
    // Clear Button
    fill(buttonClearColor)
    buttonClearWidth = (textWidth(BUTTON_CLEAR_LABEL) + (2 * buttonTextPadding)).asInstanceOf[Int]
    buttonClearPosX = buttonResolvePosX + buttonResolveWidth + buttonMargin
    rect(buttonClearPosX, buttonClearPosY, buttonClearWidth, buttonClearHeight)
    textSize(buttonFontSize)
    fill(WHITE)
    text(BUTTON_CLEAR_LABEL, buttonClearPosX + buttonTextPadding, buttonClearPosY + buttonFontSize + buttonTextPadding)
  }

  import systems.SquareMatrix
  import java.util

  def update(x: Int, y: Int): Unit = {
    buttonResolveOver = overResolveButton(buttonResolvePosX, buttonResolvePosY, buttonResolveWidth, buttonResolveHeight)
    buttonClearOver = overClearButton(buttonClearPosX, buttonClearPosY, buttonClearWidth, buttonClearHeight)
  }

  def overResolveButton(x: Int, y: Int, width: Int, height: Int): Boolean = if (mouseX >= x && mouseX <= (x + width) && mouseY >= y && mouseY <= (y + height)) {
    buttonResolveColor = BUTTON_HIGHLIGHT
    true
  }
  else {
    buttonResolveColor = BUTTON_COLOR
    false
  }

  def overClearButton(x: Int, y: Int, width: Int, height: Int): Boolean = if (mouseX >= x && mouseX <= (x + width) && mouseY >= y && mouseY <= (y + height)) {
    buttonClearColor = BUTTON_HIGHLIGHT
    true
  }
  else {
    buttonClearColor = BUTTON_COLOR
    false
  }

  override def mousePressed(): Unit = {
    if (buttonResolveOver) { // Resolution
      if (points.size >= 2) {
        smoothing
      } else {
        println("Not enough points (yet)")
      }
    }
    else if (buttonClearOver) { // Clear
      println("Clear!")
      points = new util.ArrayList[Point]
      coeffs = null
    }
    else if (!hsbDegree.overEvent) { // More here... like dropping points on canvas
      points.add(new Point(mouseX, mouseY))
      println(s"Now ${points.size} point(s) in the buffer")
    }
  }

  override def mouseDragged(): Unit = {
    points.add(new Point(mouseX, mouseY))
    println(s"Now ${points.size} point(s) in the buffer")
  }

  // Calculate the result of the function
  def func(x: Double, coeff: Array[Double]): Double = {
    var d:Double = 0.0
    val len = coeff.length
    for (i <- 0 until len) {
      d += (coeff(i) * Math.pow(x, len - 1 - i))
    }
    d
  }

  /**
    * For details on the least squares method:
    * See http://www.efunda.com/math/leastsquares/leastsquares.cfm
    * See http://www.lediouris.net/original/sailing/PolarCO2/index.html
    */
  def smoothing(): Unit = {
    val dimension = requiredSmoothingDegree + 1
    val sumXArray = new Array[Double]((requiredSmoothingDegree * 2) + 1)
    // Will fill the matrix
    val sumY = new Array[Double](requiredSmoothingDegree + 1)
    // Init
    for (i:Int <- 0 until ((requiredSmoothingDegree * 2) + 1)) {
      sumXArray(i) = 0.0
    }
    for (i:Int <- 0 until (requiredSmoothingDegree + 1)) {
      sumY(i) = 0.0
    }

    points.forEach(pt => {
      for (i:Int <- 0 until ((requiredSmoothingDegree * 2) + 1)) {
        sumXArray(i) += Math.pow(pt.x, i)
      }
      for (i:Int <- 0 until (requiredSmoothingDegree + 1)) {
        sumY(i) += (pt.y * Math.pow(pt.x, i))
      }
    })
    // Fill the matrix
    val squareMatrix = new SquareMatrix(dimension)
    for (row <- 0 until dimension) {
      for (col <- 0 until dimension) {
        val powerRnk = (requiredSmoothingDegree - row) + (requiredSmoothingDegree - col)
        println("[" + row + "," + col + ":" + powerRnk + "] = " + sumXArray(powerRnk))
        squareMatrix.setElementAt(sumXArray(powerRnk), row, col)
      }
    }
    // System coeffs
    val constants = new Array[Double](dimension)
    for (i:Int <- 0 until dimension) {
      constants(i) = sumY(requiredSmoothingDegree - i)
      println("[" + (requiredSmoothingDegree - i) + "] = " + constants(i))
    }
    println("Resolving:")
    SystemUtil.printSystem(squareMatrix, constants)
    println
    val result = SystemUtil.solveSystem(squareMatrix, constants)
    var out = "[ "
    for (i:Int <- 0 until result.length) {
      out += s"${if (i > 0) ", " else ""}${result(i)}"
    }
    out += " ]"
    println(out)
    println(s"From ${points.size} points")
    coeffs = result // For the drawing
  }
}
