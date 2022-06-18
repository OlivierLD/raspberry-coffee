package systems

object MatrixUtils {

  /**
    * The minor of matrix M, col C, row R is
    * M without column C, and without row R
    * @param matrix Original matrix to get the minor of
    * @param row the row to exclude
    * @param col the column to exclude
    * @return the expected minor
    */
  def minor(matrix: SquareMatrix, row: Int, col: Int): SquareMatrix = {
    var small = new SquareMatrix(matrix.getDimension - 1)
    for (column: Int <- 0 until matrix.getDimension) {
      if (column != col) {
        for (line: Int <- 0 until matrix.getDimension) {
          if (line != row) {
            small.setElementAt(matrix.getElementAt(line, column),
              if (line < row)  line else line -  1,
              if (column < col) column else column - 1)
          }
        }
      }
    }
    small
  }

  def determinant(matrix: SquareMatrix): Double = {
    var det = 0.0;
    if (matrix.getDimension == 1) {
      det = matrix.getElementAt(0, 0)
    } else {
       for (c:Int <- 0 until matrix.getDimension) { // c is the column in the Major
         var minorDeterminant = determinant(minor(matrix, 0, c))
         det += (matrix.getElementAt(0, c) * minorDeterminant * Math.pow(-1, c + 2))
       }
    }
    det
  }

  /**
    * The comatrix of matrix M has each of its elements(c, r)
    * replaced with the determinant of its minor (with a sign, see the code)
    *
    * @param matrix Original matrix
    * @return the expected co-matrix
    */
  def comatrix(matrix: SquareMatrix): SquareMatrix = {
    var comatrix = new SquareMatrix(matrix.getDimension)
    for (row:Int <- 0 until matrix.getDimension) {
      for (col:Int <- 0 until matrix.getDimension) {
        comatrix.setElementAt(determinant(minor(matrix, row, col)) * Math.pow(-1, row + col + 2), row, col)
      }
    }
    comatrix
  }

  /**
    * Swaps columns and rows
    * @param matrix Original Matrix
    * @return Transposed Matrix
    */
  def transposed(matrix: SquareMatrix): SquareMatrix = {
    var transposed = new SquareMatrix(matrix.getDimension)
    for (row:Int <- 0 until matrix.getDimension) {
      for (col:Int <- 0 until matrix.getDimension) {
        transposed.setElementAt(matrix.getElementAt(col, row), row, col)
      }
    }
    transposed
  }

  /**
    * Multiplies each element by a given value
    * @param matrix
    * @param by
    * @return
    */
  def multiply(matrix: SquareMatrix, by: Double):SquareMatrix = {
    var multiplied = new SquareMatrix(matrix.getDimension)
    for (row:Int <- 0 until matrix.getDimension) {
      for (col: Int <- 0 until matrix.getDimension) {
        multiplied.setElementAt(matrix.getElementAt(row, col) * by, row, col)
      }
    }
    multiplied
  }

  /**
    * Inverted matrix is the transposed of the comatrix, multiplied by the inverse of its own determinant
    * @param matrix Original Matrix
    * @return Inverted one
    */
  def invert(matrix:SquareMatrix):SquareMatrix = {
    multiply(transposed(comatrix(matrix)), (1.0 / determinant(matrix)))
  }
}

class SquareMatrix (dim: Int) {

  if (dim < 1) {
    throw new IllegalArgumentException("Dimension must be at least 1")
  }
  private var dimension: Int = dim
  private var elements: Array[Array[Double]] = Array.ofDim[Double](dim, dim)

  def this(dim: Int, elements: Double*) = {
    this(dim)
    if (elements == null) {
      throw new IllegalArgumentException("Elements array cannot be null")
    }
    if (elements.length != (dimension * dimension)) {
      throw new IllegalArgumentException(s"Invalid elements array length for dimension ${dimension}. Expected ${dimension * dimension}, got ${elements.length}")
    }
    for (line:Int <- 0 until this.dimension) {
      for (col:Int <- 0 until this.dimension) {
        this.elements(line)(col) = elements((line * dimension) + col);
      }
    }
  }

  def getDimension : Int = {
    this.dimension
  }

  def setElementAt(value: Double, row:Int, col:Int): Unit = {
    this.elements(row)(col) = value
  }

  def getElementAt(row:Int, col:Int): Double = {
    this.elements(row)(col)
  }

  def display: Unit = {
    for (line:Int <- 0 until this.dimension) {
      var str = "| "
      for (col:Int <- 0 until this.dimension) {
        str += (s"${this.elements(line)(col)} ")
      }
      str += "|"
      println(str)
    }
  }
}

object SystemUtils {

  /**
    * Solves a system, n equations, n unknowns.
    * <p>
    * the values we look for are x, y, z.
    * <p>
    * ax + by + cz = X
    * Ax + By + Cz = Y
    * Px + Qy + Rz = Z
    *
    * @param matrix Coeffs matrix, n x n (left)
    *          | a b c |
    *          | A B C |
    *          | P Q R |
    * @param constants Constants array, n (right) [X, Y, Z]
    * @return the unknown array, n. [x, y, z]
    */
  def solveSystem(matrix:SquareMatrix, constants:Array[Double]): Array[Double] = {
    var result = Array.ofDim[Double](matrix.getDimension)
    var inverted = MatrixUtils.invert(matrix)
    // Lines by column
    for (row:Int <- 0 until matrix.getDimension) {
      result(row) = 0.0
      for (col:Int <- 0 until matrix.getDimension) {
        result(row) += (inverted.getElementAt(row, col) * constants(col))
      }
    }
    result
  }

  def printSystem(squareMatrix: SquareMatrix, constants: Array[Double]) = {
    val unknowns = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val dimension = squareMatrix.getDimension
    for (row:Int <- 0 until dimension) {
      var line = ""
      for (col:Int <- 0 until dimension) {
        line += s"${if (line.trim.length > 0)  " + " else ""}(${squareMatrix.getElementAt(row, col)} x ${unknowns.charAt(col)})"
      }
      line += s" = ${constants(row)}"
      println(line)
    }
  }

  def main(args: Array[String]): Unit = {

    var sqMat = new SquareMatrix(3, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    sqMat.display
    println()
    // Samples...
    MatrixUtils.minor(sqMat, 2, 2).display

    println(s"Determinant: ${MatrixUtils.determinant(MatrixUtils.minor(sqMat, 2, 2))}")

    println("--- System resolution ---")
    sqMat = new SquareMatrix(3, 12, 13, 14, 1.345, -654, 0.001, 23.09, 5.3, -12.34)
    val constants:Array[Double] = Array(234 , 98.87 , 9.876)
    val before = System.nanoTime()
    val result = solveSystem(sqMat, constants)
    val after = System.nanoTime()
    println(s"Resolved in ${  java.text.NumberFormat.getNumberInstance().format(after - before) } nano sec.")
    printSystem(sqMat, constants)

    println(s"A = ${result(0)}")
    println(s"B = ${result(1)}")
    println(s"C = ${result(2)}")
  }
}