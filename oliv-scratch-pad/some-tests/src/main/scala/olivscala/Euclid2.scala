package olivscala

object Euclid2 {

  private def swap(u: Int, v: Int): (Int, Int) = {
    return (v, u)
  }

  private def greatestCommonDivisor(u: Int, v: Int) = { // PGCD
    var _u = u
    var _v = v

    var nb = 0

    while (_u > 0) {
      if (_u < _v) {
        println(s"Swapping U:${_u} and V:${_v}")
        val tuple = swap(_u, _v)
        _u = tuple._1
        _v = tuple._2
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
