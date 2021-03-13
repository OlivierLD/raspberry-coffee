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
```
To run an example:
```
$ go run .
Let's get started!
DeltaT for 2020-3: 71.71293632812495 s
Calculating for 2020-3-28 16:50:20
Julian Dates 2.458938e+06 2.45893720162037e+06 2.458937202450381e+06
Sideral Time 5h 16m 35.526s
EoT: -4.879415188586677 => - 4m 52.8s
------------- Bodies --------------
Sun 	GHA: 71° 21' 49", RA: 0h 31m 7.2s, Dec: N 3°21'30", sd: 961.200000", hp: 8.800000"
Venus 	GHA: 27° 59' 22", RA: 3h 24m 37s, Dec: N 22°6'49", sd: 12.400000", hp: 12.900000"
Mars 	GHA: 138° 18' 9", RA: -4h 3m 21.8s, Dec: S 21°20'27", sd: 3.100000", hp: 5.900000"
Jupiter 	GHA: 143° 16' 28", RA: -5h 43m 28.6s, Dec: S 21°22'2", sd: 18.300000", hp: 1.600000"
Saturn 	GHA: 136° 28' 7", RA: -4h 10m 42s, Dec: S 20°6'17", sd: 8.000000", hp: 0.800000"
Moon 	GHA: 25° 9' 39", RA: 3h 35m 55.9s, Dec: N 16°11'24", sd: 894.400000", hp: 3282.300000"
	Moon Phase: 47.1,  +cre
Polaris	GHA: 35° 17' 2", RA: 2h 55m 26.3s, Dec: N 89°21'2"
Ecliptic obliquity 23° 26' 11.973000", true 23° 26' 12.013000"
Lunar Distance 47° 10' 4"
Day of Week SAT
---------------------------------------
Done!
$ 
```

## Good resource
- <https://tour.golang.org/>
- Math package <https://golang.org/pkg/math/>
- Fmt package <https://golang.org/pkg/fmt/>
- Exported/Unexported <https://golangbyexample.com/exported-unexported-fields-struct-go/>

---
