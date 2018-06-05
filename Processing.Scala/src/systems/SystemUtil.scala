package systems

object SystemUtil {

  def solveSystem(m: SquareMatrix, c: Array[Double]): Array[Double] = {
    var result = new Array[Double](m.getDimension)
    val inv = MatrixUtils.invert(m)
    // Lines * Column
    for (row <- 0 until m.getDimension) {
      result(row) = 0.0
      for (col <- 0 until m.getDimension) {
        result(row) += (inv.getElementAt(row, col) * c(col))
      }
    }
    result
  }

  def printSystem(squareMatrix: SquareMatrix, constants: Array[Double]): Unit = {
    val unknowns = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val dimension = squareMatrix.getDimension
    for (row <- 0 until dimension) {
      var line = ""
      for (col <- 0 until dimension) {
        line += s"${if (line.trim.length > 0) " + " else ""}(${squareMatrix.getElementAt(row, col)} x ${unknowns.charAt(col)})"
      }
      line += s" = ${constants(row)}"
      println(line)
    }
  }
}
