package main

import (
	"fmt"
	"oliv.astro/celest"
)

func main() {
	message := celest.Hello("World")
	fmt.Println(message)
}
