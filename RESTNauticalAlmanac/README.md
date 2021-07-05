# Nautical Almanac primitives 
### also provides REST resources implementation

--- 

For the value of Delta T, see:
- http://maia.usno.navy.mil/
- http://maia.usno.navy.mil/ser7/deltat.data

> _Note_: the above stopped working, before 2020... We now have a way to _calculate_ `DeltaT`.

---

Designed to be part of other projects, see `RESTNavServer`, `Project.Trunk:SunFlower`, `RESTTideEngine`, ...

---

## Build it
To run it in standalone (which is by far not the only way), you need to build it first:
```
 $ ../gradlew clean shadowJar
```
Then, you can give it a try:
```
 $ java -cp ./build/libs/RESTNauticalAlmanac-1.0-all.jar implementation.almanac.AlmanacComputer -help
```
or something like
```
 $ java -cp ./build/libs/RESTNauticalAlmanac-1.0-all.jar implementation.almanac.AlmanacComputer -type continuous -year 2019 -month 10 -day 28
```

---
