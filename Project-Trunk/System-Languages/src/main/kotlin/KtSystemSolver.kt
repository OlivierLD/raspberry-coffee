package systemsKt

/**
 * This is an example, illustrating the basics of Kotlin.
 *
 * System resolution.
 *
 * Featured:
 * - Constructor override
 * - Recursion
 * - Enums
 * - Casts
 * - Loops
 * - Unicode characters
 * - ...
 */
class SquareMatrix(dim: Int) {
	val dimension = dim
	private val elements = Array(dim) { DoubleArray(dim) }

	constructor(dim: Int, elements: DoubleArray) : this(dim) {
		this.set(elements)
	}

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
		val small = SquareMatrix(matrix.dimension - 1)

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
				det += (matrix.get(0, col) * minorDeterminant * Math.pow(-1.toDouble(), (col + 2).toDouble()))
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
		val det = determinant(matrix)
		if (det == 0.0) {
			throw RuntimeException("Determinant is null")
		}
		return multiply(transposed(comatrix(matrix)), 1.0 / det)
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
		result.forEachIndexed { index, _ -> result[index] = 0.0 }

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
		val unknowns = unknowns.GREEK_LOWS.charArray
		for (line in 0..(matrix.dimension - 1)) {
			var str = ""
			for (col in (0..(matrix.dimension - 1))) {
				str += "${if (str.trim().length > 0) " + " else ""}(${matrix.get(line, col)} . ${unknowns[col]})"
			}
			str += " = ${constants[line]}"
			println(str)
		}
	}
}

enum class unknowns(val charArray: CharArray) {
	LATIN_CAPS("ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()),
	LATIN_LOWS("abcdefghijklmnopqrstuvwxyz".toCharArray()),
	GREEK_CAPS("\u0391\u0392\u0393\u0394\u0395\u0396\u0397\u0398\u0399\u039A\u039B\u039C\u039D\u039E\u039F\u03A0\u03A1\u03A2\u03A3\u03A4\u03A5\u03A6\u03A7\u03A8\u03A9".toCharArray()),
	GREEK_LOWS("\u03b1\u03b2\u03b3\u03b4\u03b5\u03b6\u03b7\u03b8\u03b9\u03ba\u03bb\u03bc\u03bd\u03be\u03bf\u03c0\u03c1\u03c2\u03c3\u03c4\u03c5\u03c6\u03c7\u03c8\u03c9".toCharArray())
}

val MICRO_SYMBOL = unknowns.GREEK_LOWS.charArray[11]

// Just a test
fun main(args: Array<String>) {
	println("-- Matrix tests, in Kotlin --")
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

	val smallMatrix = SquareMatrix(2, doubleArrayOf(1.0, 1.0, 1.0, 1.0))
	MatrixUtils.display(smallMatrix)
	println("Determinant: ${MatrixUtils.determinant(smallMatrix)}")
	println("--- System resolution ---")
	try {
		val constants = doubleArrayOf(1.0, 1.0)
		SystemUtils.printSystem(smallMatrix, constants)
		SystemUtils.solveSystem(smallMatrix, constants)
	} catch (ex: Exception) {
		println(">>> No solution -> ${ex.toString()}")
	}

	println("--- System resolution ---")
	matrix.set(doubleArrayOf(12.toDouble(), 13.toDouble(), 14.toDouble(), 1.345, -654.toDouble(), 0.001, 23.09, 5.3, -12.34))
	val constants = doubleArrayOf(234.toDouble(), 98.87, 9.876)
	val before = System.nanoTime()
	val result = SystemUtils.solveSystem(matrix, constants)
	val after = System.nanoTime()
	println("Resolved in ${java.text.NumberFormat.getNumberInstance().format(after - before)} nano sec (${java.text.NumberFormat.getNumberInstance().format((after - before) / 1000)} ${MICRO_SYMBOL}s).")
	SystemUtils.printSystem(matrix, constants)

	val unknowns = unknowns.GREEK_LOWS.charArray
	result.forEachIndexed({ index, d -> println("${unknowns[index]} = ${d}") })
}
