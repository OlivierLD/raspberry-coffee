/*
 * Calculate the length of a curve.
 * Provide the coeff of the polynomial in the 'coeff' array.
 * Also tune the 'inc' (increment) value.
 */

def pythagore(a:Double, b:Double):Double = {
  Math.sqrt((a*a) + (b*b))
}

def lenBetween(p1:(Double, Double), p2:(Double, Double)):Double = {
  var a = p1._1 - p2._1
  var b = p1._2 - p2._2

  pythagore(a, b)
}

// y=-0.0061x²+0.0029x+4.6
val coeff = Array[Double](0.0061, 0.0029, 4.6)
// val coeff = Array[Double](1, 0)

def f(coeffs:Array[Double], x:Double): Double = {
  var y:Double = 0

  for (c <- 0 to (coeffs.length - 1)) {
    val exp = coeffs.length - c - 1
    y += (coeffs(c) * (Math.pow(x, exp)))
  }
  y
}

var len:Double = 0;
var x:Double = -10;
var inc = 0.1;
val max:Double = 10
var prev:(Double, Double) = null
while (x <= max) {
  val y = f(coeff, x)
  if (prev != null) {
    val small = lenBetween((prev._1, prev._2), (x, y))
    len += small
//  println(s"Length between (${prev._1}, ${prev._2}) and (${x}, ${y}): ${small}")
  }
  prev = (x, y)
  x += inc;
}

println(s"Length is ${len}")

