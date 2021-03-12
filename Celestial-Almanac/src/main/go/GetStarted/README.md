```
$ mkdir celest
$ cd celest
$ go mod init oliv.astro/celest
go: creating new go.mod: module oliv.astro/celest
```
Then, in the celest directory, create a `celest.go` as follow:
```go
package celest

import "fmt"

func Hello(name string) string {
	// Return a greeting that embeds the name in a message.
    message := fmt.Sprintf("Hi, %v. Welcome!", name)
    return message
}
```

Then:
```
$ cd ..
$ mkdir main
$ cd main
$ go mod init oliv.astro/main
```
Create the a `main.go` in the `main` folder:
```go
package main

import (
	"fmt"
	"oliv.astro/celest"
)

func main() {
	message := celest.Hello("World")
	fmt.Println(message)
}
```

Almost there:
```
$ cd main
$ go mod edit -replace=oliv.astro/celest=../celest
```
and
```
$ go mod tidy
go: found oliv.astro/celest in oliv.astro/celest v0.0.0-00010101000000-000000000000
```

And finally:
```
$ go run .
Hi, World. Welcome!
```
