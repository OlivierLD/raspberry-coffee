object Fractions {

  def main(args:Array[String]):Unit = {
//  val num = Math.PI  // 3.1415926
    val num = 0.515625
    val dec = num.toString.length - num.toString.indexOf('.')
    val den = Math.pow(10, dec).asInstanceOf[Long]
    val frac = reduce((num * den).asInstanceOf[Long], den)
    println(s"${num} = ${frac._1}/${frac._2}")
    println(s"Proof:${frac._1}/${frac._2} = ${ frac._1.asInstanceOf[Double] / frac._2.asInstanceOf[Double] }" )
  }

  def reduce(fr:(Long, Long)):(Long, Long) = {
    var n1 = fr._1
    var n2 = fr._2
    val t1 = n1
    val t2 = n2

    while (n1 != n2) {
      if (n1 > n2)
        n1 -= n2
      else
        n2 -= n1
    }
    (t1 / n1, t2 / n1)
  }
}
