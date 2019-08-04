WIP 

## To compile
```
$ ./gradlew build
```

This project depends on other projects.

> Note:

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

## To run the SpringBoot server
```
$ java -jar build/libs/sensors-spring-boot-0.1.0.jar
```
The `META-INF/MANIFEST.MF` looks like this:
```manifest.mf
Manifest-Version: 1.0
Main-Class: org.springframework.boot.loader.JarLauncher
Start-Class: rpi.Application
Spring-Boot-Version: unknown
Spring-Boot-Classes: BOOT-INF/classes/
Spring-Boot-Lib: BOOT-INF/lib/
```

## To use the services
<!--
```
GET http://localhost:8080/actuator/health
```
-->
```
GET http://localhost:8080/light/ambient
```

```
GET http://localhost:8080/relay/status/1
```

```
POST http://localhost:8080/relay/status/1
{ "status": true }
```
