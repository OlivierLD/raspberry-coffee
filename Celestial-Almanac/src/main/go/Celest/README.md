# How to...

```
$ mkdir main
$ mkdir astro
```

```
$ cd astro
$ go mod init oliv.cc/astro
go: creating new go.mod: module oliv.cc/astro
```

```
$ cd ../main
$ go mod init oliv.cc/main
go: creating new go.mod: module oliv.cc/main
```

Create main...
```
$ cd main
$ go mod edit -replace=oliv.cc/astro=../astro
$ go mod tidy
go: found oliv.cc/astro in oliv.cc/astro v0.0.0-00010101000000-000000000000
$ go run .
```

## Good resource
- <https://tour.golang.org/>
- Math package <https://golang.org/pkg/math/>
- Fmt package <https://golang.org/pkg/fmt/>
- Exported/Unexported <https://golangbyexample.com/exported-unexported-fields-struct-go/>

---
