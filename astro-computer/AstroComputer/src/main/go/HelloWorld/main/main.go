package main

import (
	"fmt"
	"oliv.tuto/greetings"
)

func main() {
	englishMessage := greetings.Hello("World")
	fmt.Println(englishMessage)

	frenchMessage := greetings.Salut("Monde")
	fmt.Println(frenchMessage)
}
