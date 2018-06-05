package systems

import processing.core._

class HScrollBar(val xpos: Double, val yp: Double, val swidth: Int, val sheight: Int // width and height of bar
                 , val loose: Int // how loose/heavy
                )  extends PApplet {
  
  val widthtoheight: Int = swidth - sheight
  ratio = swidth.toFloat / widthtoheight.toFloat
  ypos = yp - sheight / 2
  spos = xpos + swidth / 2 - sheight / 2
  newspos = spos
  sposMin = xpos
  sposMax = xpos + swidth - sheight
  var ypos = .0 // x and y position of bar

  var spos = .0
  var newspos = .0 // x position of slider

  var sposMin = .0
  var sposMax = .0 // max and min values of slider

  var over = false // is the mouse over the slider?

  var locked = false
  var ratio = .0

  def update(): Unit = {
    if (overEvent) {
      over = true
    } else {
      over = false
    }
    if (mousePressed && over) {
      locked = true
    }
    if (!mousePressed) {
      locked = false
    }
    if (locked) {
      newspos = constrain(mouseX - sheight / 2, sposMin.asInstanceOf[Float], sposMax.asInstanceOf[Float])
    }
    if (Math.abs(newspos - spos) > 1) {
      spos = spos + (newspos - spos) / loose
    }
  }

  def constrain(value: Float, minv: Float, maxv: Float): Float = Math.min(Math.max(value, minv), maxv)

  def overEvent: Boolean = if (mouseX > xpos && mouseX < xpos + swidth && mouseY > ypos && mouseY < ypos + sheight) true
  else false

  def display(): Unit = {
    noStroke
    fill(204, 128)
    rect(xpos.asInstanceOf[Float], ypos.asInstanceOf[Float], swidth, sheight)
    if (over || locked) fill(0, 0, 0)
    else fill(102, 102, 102)
    rect(spos.asInstanceOf[Float], ypos.asInstanceOf[Float], sheight, sheight)
  }

  def getPos: Double = { // Convert spos to be values between
    // 0 and the total width of the scrollbar
    spos * ratio
  }

  def setPos(pos: Double): Unit = {
    newspos = pos
  }
}
