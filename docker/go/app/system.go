// Do see https://tour.golang.org/
package main

import (
	"fmt"
	"math"
)

func printMatrix(matrix [][]float64) {
	var size = len(matrix)
	var r, c int
	for r=0; r<size; r++ {
		fmt.Print("| ")
		for c=0; c<size; c++ {
			fmt.Printf(" %v", matrix[r][c])
		}
		fmt.Println(" |")
	}
}

func multiply(matrix [][]float64, n float64) [][]float64 {
	var res [][]float64
	// min is a slice
	var size = len(matrix)
	res = make([][]float64, size, size)
	var r, c int
	for r=0; r<size; r++ {
		res[r] = make([]float64, size, size)
	}

	for r=0; r<size; r++ {
		for c=0; c<size; c++ {
			res[r][c] = n * matrix[r][c]
		}
	}
	return res
}

func comatrix(matrix [][]float64) [][]float64 {
	var comat [][]float64
	// min is a slice
	var size = len(matrix)
	comat = make([][]float64, size, size)
	var r, c int
	for r=0; r<size; r++ {
		comat[r] = make([]float64, size, size)
	}

	for r=0; r<size; r++ {
		for c=0; c<size; c++ {
			comat[r][c] = determinant(minor(matrix, r, c)) * math.Pow(-1.0, float64(r + c + 2))
		}
	}
	return comat
}

func transposed(matrix [][]float64) [][]float64 {
	var transp [][]float64
	// min is a slice
	var size = len(matrix)
	transp = make([][]float64, size, size)
	var r, c int
	for r=0; r<size; r++ {
		transp[r] = make([]float64, size, size)
	}

	for r=0; r<size; r++ {
		for c=0; c<size; c++ {
			transp[r][c] = matrix[c][r]
		}
	}
	return transp
}

func invert(matrix [][]float64) [][]float64 {
	return multiply(transposed(comatrix(matrix)), (1.0 / determinant(matrix)))
}

func minor(matrix [][]float64, row int, col int) [][]float64 {

	var min [][]float64
	// min is a slice
	var size = len(matrix)
	min = make([][]float64, size - 1, size - 1)
	var r, c int
	for r=0; r<(size - 1); r++ {
		min[r] = make([]float64, size - 1, size - 1)
	}

	for r=0; r<size; r++ {
		if r != row {
			for c=0; c<size; c++ {
				if c != col {
					var mr, mc int
					if r < row {
						mr = r
					} else {
						mr = r - 1
					}
					if c < col {
						mc = c
					} else {
						mc = c - 1
					}
					min[mr][mc] = matrix[r][c]
				}
			}
		}
	}
	return min
}

func determinant(matrix [][]float64) float64 {
	var value float64 = 0

	if len(matrix) == 1 {
		value = matrix[0][0]
	} else {
		// C: column in Major
		var C int
		for C=0; C<len(matrix); C++ {
			var minDet = determinant(minor(matrix, 0, C))
			value += (matrix[0][C] * minDet * math.Pow(-1.0, float64(C + 1 + 1)))
		}
	}
	return value
}

func solveSystem(matrix [][]float64, coeffs []float64) []float64 {
	var size = len(coeffs)
	var result []float64
	result = make([]float64, size, size)
	var inverted = invert(matrix)
	var r, c int
	for r=0; r<size; r++ {
		result[r] = 0.0
		for c=0; c<size; c++ {
			result[r] += (inverted[r][c] * coeffs[c])
		}
	}
	return result
}

func printSystem(matrix [][]float64, coeffs []float64) {
	const UNKNOWNS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
	var size = len(matrix)
	var r, c int
	for r=0; r<size; r++ {
		for c=0; c<size; c++ {
			if c > 0 {
				fmt.Print(" + ")
			}
			fmt.Printf("(%v x %c)", matrix[r][c], UNKNOWNS[c])
		}
		fmt.Printf(" = %v\n", coeffs[r])
	}
}

func main() {
	matrix := [][]float64 {
		{  12.0, 13.0, 14.0 },
		{ 1.345, -654, 0.001 },
		{ 23.09, 5.3, -12.34 }}

	coeffs := []float64 { 234, 98.87, 9.876 }

	fmt.Println("Resolving:")
	printSystem(matrix, coeffs)

	var result = solveSystem(matrix, coeffs)
	fmt.Printf("A = %v\n", result[0])
	fmt.Printf("B = %v\n", result[1])
	fmt.Printf("C = %v\n", result[2])
}
