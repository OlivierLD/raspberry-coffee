object EuclidAlgorithm {

  private def greatestCommonDivisor(u: Int, v: Int) = { // PGCD
    var t = 0
    var _u = u
    var _v = v
    while (_u > 0) {
      if (_u < _v) {
        t = _u
        _u = _v
        _v = t
      }
      _u -= _v
    }
    _v
  }

  def main(args: Array[String]): Unit = {
    var x = 0
    var y = 0
    x = 4692170
    y =   16915
    val gcd = greatestCommonDivisor(x, y)
    println(s"GCD(${x}, ${y}) = ${gcd}")
    println(s"=> ${x} / ${y} = ${ x / gcd } / ${ y / gcd }")
  }
}
