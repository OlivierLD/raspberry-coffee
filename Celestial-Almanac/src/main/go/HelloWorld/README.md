# Get Started, quick
```
$ mkdir greetings
$ cd greetings
$ go mod init oliv.tuto/greetings
go: creating new go.mod: module oliv.tuto/greetings
```
Then, in `greetings`, create a, `english.go`:
```go
package greetings

import "fmt"

func Hello(name string) string {
	// Return a greeting that embeds the name in a message.
    message := fmt.Sprintf("Hi, %v. Welcome!", name)
    return message
}
```
and a `french.go`:
```go
package greetings

import "fmt"

func Salut(name string) string {
	// Return a greeting that embeds the name in a message.
    message := fmt.Sprintf("Salut, %v. Bienvenue !", name)
    return message
}
```

Then:
```
$ cd ..
$ mkdir main
$ cd main
$ go mod init oliv.tuto/main
go: creating new go.mod: module oliv.tuto/main
```
In the `main` folder, create a `main.go`:
```go
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
```
Almost there:
```
$ cd main
$ go mod edit -replace=oliv.tuto/greetings=../greetings
```
and 
```
$ go mod tidy
go: found oliv.tuto/greetings in oliv.tuto/greetings v0.0.0-00010101000000-000000000000
```
And finally:
```
go run .
Hi, World. Welcome!
Salut, Monde. Bienvenue !
```
