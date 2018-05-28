// WIP. Do see https://tour.golang.org/
package main

import "fmt"

func minor(matrix [][]float64, row int, col int) [][]float64 {

	min := [2][2]float64 { {0,0}, {0,0} }

	return min
}

func main() {
	matrix := [3][3]float64 {
		{  12.0, 13.0, 14.0 },
		{ 1.345, -654, 0.001 },
		{ 23.09, 5.3, -12.34 }}

	fmt.Println(matrix)
}
