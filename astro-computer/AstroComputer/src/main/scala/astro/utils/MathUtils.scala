package astro.utils

object MathUtils {

  /**
   * Sine of an angle in degrees
   */
  def sinD(x: Double): Double = Math.sin(Math.toRadians(x))

  /**
    * Cosine of an angle in degrees
    */
  def cosD(x: Double): Double = Math.cos(Math.toRadians(x))

  /**
   * Tangent of an angle in degrees
   */
  def tanD(x: Double): Double = Math.tan(Math.toRadians(x))

  def norm360Deg(x: Double): Double = {
    var _x = x
    while (_x < 0) {
      _x += 360
    }
    while (_x > 360) {
      _x -= 360
    }
    _x
  }

  def norm2PiRad(x: Double): Double = {
    var _x = x
    while (_x < 0) {
      _x += (2 * Math.PI)
    }
    while (_x > (2 * Math.PI)) {
      _x -= (2 * Math.PI)
    }
    _x
  }

  /**
    * Cosine of a normalized angle in radians
    * @param x in radians
    * @return
    */
  def cosT(x: Double): Double = Math.cos(norm2PiRad(x))

  def trunc(x: Double): Double = 360 * (x / 360 - Math.floor(x / 360))

  def trunc2(x: Double): Double = (2D * Math.PI) * (x / (2D * Math.PI) - Math.floor(x / (2D * Math.PI)))

}