package main

import "fmt"

func swap (x, y string) ( string, string) {
	return y, x // returns a tuple
}

func main() {
	a, b := swap("Akeu", "Coucou")
	fmt.Println(a, b)
}
