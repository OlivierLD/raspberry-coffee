WIP 

To compile:
```
$ ./gradlew build
```

This project depends on other projects.
If you see errors like this:
```
./gradlew build
FAILURE: Build failed with an exception.

* What went wrong:
Could not resolve all files for configuration ':Project.Trunk:REST.clients:REST.assembler:springboot-sensors:compileClasspath'.
> Could not resolve oliv.raspi.coffee:RMI.sample:1.0.
  Required by:
      project :Project.Trunk:REST.clients:REST.assembler:springboot-sensors > oliv.raspi.coffee:I2C.SPI:1.0
   > Skipped due to earlier error

```
then you need to `install` (Maven) the dependencies in your local Maven repo.
For example, in the case of the error above, you do:
```
$ cd .../RMI.sample
$ ../gradlew install
```
Same for all the dependency errors you would see.

To run:
```
$ java -jar build/libs/sensors-spring-boot-0.1.0.jar
```
