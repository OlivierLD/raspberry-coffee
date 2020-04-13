package olivscala

object Euclid {

  private def greatestCommonDivisor(u: Int, v: Int) = { // PGCD
    var t = 0
    var _u = u
    var _v = v

    var nb = 0

    while (_u > 0) {
      if (_u < _v) {
        println(s"Swapping U:${_u} and V:${_v}")
        t = _u
        _u = _v
        _v = t
        println(s"    >>> U:${_u} and V:${_v}")
      }
      _u -= _v
      nb += 1
      println(s"${nb} U now ${_u}")
    }
    _v
  }

  def main(args: Array[String]): Unit = {
    val x = 4692170
    val y = 16915
    val gcd = greatestCommonDivisor(x, y)
    println(s"GCD(${x}, ${y}) = ${gcd}")
    println(s"=> ${x} / ${y} = ${ x / gcd } / ${ y / gcd }")
  }
}
