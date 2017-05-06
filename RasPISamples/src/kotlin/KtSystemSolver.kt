package systemsKt

class SquareMatrix(dim: Int) {
    val dimension = dim
    val elements = Array(dim, { DoubleArray(dim) })

    fun get(row: Int, col: Int): Double {
        return elements[row][col]
    }

    fun set(row: Int, col: Int, value: Double) {
        elements[row][col] = value
    }

    fun set(elmts: DoubleArray) {
        if (elmts.size != (dimension * dimension)) {
            throw RuntimeException("Array size should be ${dimension * dimension} instead of ${elmts.size}")
        }
        for (line in 0..(dimension - 1)) {
            for (column in 0..(dimension - 1)) {
                elements[line][column] = elmts[(line * dimension) + column]
            }
        }
    }
}

object MatrixUtils {
    fun minor(matrix: SquareMatrix, row: Int, col: Int): SquareMatrix {
        var small = SquareMatrix(matrix.dimension - 1)

        for (line in 0..(matrix.dimension - 1)) {
            if (line != row) {
                for (column in 0..(matrix.dimension - 1)) {
                    if (column != col) {
                        small.set(
                                if (line < row) line else line - 1,
                                if (column < col) column else column - 1,
                                matrix.get(line, column))
                    }
                }
            }
        }
        return small
    }

    fun determinant(matrix: SquareMatrix): Double {
        var det = 0.0
        if (matrix.dimension == 1) {
            det = matrix.get(0, 0)
        } else {
            for (col in 0..(matrix.dimension - 1)) { // col: column in the major
                var minorDeterminant = determinant(minor(matrix, 0, col))
                det += (matrix.get(0, col) * minorDeterminant * Math.pow(-1.toDouble(), (col+2).toDouble()))
            }
        }
        return det
    }

    fun comatrix(matrix: SquareMatrix): SquareMatrix {
        var comatrix = SquareMatrix(matrix.dimension)
        for (line in 0..(matrix.dimension - 1)) {
            for (column in 0..(matrix.dimension - 1)) {
                comatrix.set(line, column, determinant(minor(matrix, line, column)) * Math.pow(-1.toDouble(), (line + column + 2).toDouble()))
            }
        }
        return comatrix
    }

    fun transposed(matrix: SquareMatrix): SquareMatrix {
        var transposed = SquareMatrix(matrix.dimension)
        for (line in 0..(matrix.dimension - 1)) {
            for (column in 0..(matrix.dimension - 1)) {
                transposed.set(line, column, matrix.get(column, line))
            }
        }
        return transposed
    }

    fun multiply(matrix: SquareMatrix, by: Double): SquareMatrix {
        var multiplied = SquareMatrix(matrix.dimension)
        for (line in 0..(matrix.dimension - 1)) {
            for (column in 0..(matrix.dimension - 1)) {
                multiplied.set(line, column, matrix.get(line, column) * by)
            }
        }
        return multiplied
    }

    fun invert(matrix: SquareMatrix): SquareMatrix {
        return multiply(transposed(comatrix(matrix)), 1.0 / determinant(matrix))
    }

    fun display(matrix: SquareMatrix) {
        for (line in 0..(matrix.dimension - 1)) {
            var lineStr = "| "
            for (col in (0..(matrix.dimension - 1))) {
                lineStr += "${matrix.get(line, col)} "
            }
            lineStr += "|"
            println(lineStr)
        }
    }
}

object SystemUtils {
    fun solveSystem(matrix: SquareMatrix, constants: DoubleArray): DoubleArray {
        var result = DoubleArray(matrix.dimension)
        result.forEachIndexed { index, d -> result[index] = 0.0 }

        var inverted = MatrixUtils.invert(matrix)
        for (line in 0..(matrix.dimension - 1)) {
//          result[line] = 0.0
            for (col in (0..(matrix.dimension - 1))) {
                result[line] += (inverted.get(line, col) * constants[col])
            }
        }
        return result
    }

    fun printSystem(matrix: SquareMatrix, constants: DoubleArray) {
        val unknowns = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
        for (line in 0..(matrix.dimension - 1)) {
            var str = ""
            for (col in (0..(matrix.dimension - 1))) {
                str += "${if (str.trim().length > 0) " + " else ""}(${matrix.get(line, col)} x ${unknowns[col]})"
            }
            str += " = ${constants[line]}"
            println(str)
        }
    }
}

// Just a test
fun main(args: Array<String>) {
    println("Matrix tests")
    var matrix = SquareMatrix(3)
    matrix.set(0, 0, 1.0)
    matrix.set(0, 1, 2.0)
    matrix.set(0, 2, 3.0)
    matrix.set(1, 0, 4.0)
    matrix.set(1, 1, 5.0)
    matrix.set(1, 2, 6.0)
    matrix.set(2, 0, 7.0)
    matrix.set(2, 1, 8.0)
    matrix.set(2, 2, 9.0)
    MatrixUtils.display(matrix)
    println()
    MatrixUtils.display(MatrixUtils.minor(matrix, 2, 2))
    println()
    println("Determinant: ${MatrixUtils.determinant(MatrixUtils.minor(matrix, 2, 2))}")

    println("--- System resolution ---")
    matrix.set(doubleArrayOf(12.toDouble(), 13.toDouble(), 14.toDouble(), 1.345, -654.toDouble(), 0.001, 23.09, 5.3, -12.34))
    val constants = doubleArrayOf(234.toDouble() , 98.87 , 9.876)
    val before = System.nanoTime()
    val result = SystemUtils.solveSystem(matrix, constants)
    val after = System.nanoTime()
    println("Resolved in ${  java.text.NumberFormat.getNumberInstance().format(after - before) } nano sec.")
    SystemUtils.printSystem(matrix, constants)

    val unknowns = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
    result.forEachIndexed( { index, d -> println("${unknowns[index]} = ${d}") } )
}