package main

import "fmt"

func main() {
	var a int = 4
	var b int32
	var c float32
	var ptr *int

	fmt.Printf("Line 1 - trype of variable a = %T\n", a);
	fmt.Printf("Line 2 - trype of variable b = %T\n", b);
	fmt.Printf("Line 3 - trype of variable c = %T\n", c);

	ptr = &a
	fmt.Printf("Value of a is %d\n", a);
	fmt.Printf("*ptr is %d.\n", *ptr);
}
