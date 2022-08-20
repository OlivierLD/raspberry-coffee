# Monitor
An example: Monitor CPU temperature and CPU load, with a Swing GUI, on a Raspberry Pi.  
Uses components from the `Algebra` module.


## Build and run
Do a 
```
$ ../../gradlew shadowJar
```
and then a 
```
$ ./run.sh --verbose:true|false|y|n --buffer-length:900 --between-loops:1000
```
CLI parameters `--verbose`, `--buffer-length`, and `--between-loops` are optional.
- Default value for `--verbose` is `false`
- Default value for `--buffer-length` is `900` items
- Default value for `--between-loops` if `1000` milliseconds.


---
