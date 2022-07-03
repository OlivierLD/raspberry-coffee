# [DIOZERO](https://www.diozero.com/)

Another option than `PI4J`. Let's give it a try.  
Seems not to require any system library. Interesting.

- diozero [git repo](https://github.com/mattjlewis/diozero)
- Samples: <https://github.com/mattjlewis/diozero/tree/main/diozero-core/src/main/java/com/diozero/devices>

> We also want to make sure this work on other JVM Languages too.  
> Install `sdkman`: `curl -s get.sdkman.io | bash`  
> `$ source ~/.sdkman/bin/sdkman-init.sh`
- Scala (install [Scala](https://sdkman.io/sdks#scala))
- Kotlin (install [Kotlin](https://sdkman.io/sdks#kotlin))
- Groovy (install [groovy](https://sdkman.io/sdks#groovy), or [here](https://groovy-lang.org/install.html))
  - Install a given version: `sdk install groovy 3.0.10` 

Build this first sample, from the `diozero-101` folder, do a: 
```
$ ../gradlew shadowJar
```
Then, for Java:
```
$ java -jar build/libs/diozero-101-1.0-all.jar
$ java -cp build/libs/diozero-101-1.0-all.jar diozerotests.SystemInformation
```
For Kotlin:
```
$ kotlin -cp build/libs/diozero-101-1.0-all.jar diozerokt.FirstTestKt        # Kt added because it is a script.
$ kotlin -cp build/libs/diozero-101-1.0-all.jar diozerokt.SystemInformation 
```
For Scala:
```
$ scala -cp build/libs/diozero-101-1.0-all.jar DioZero
```
For Groovy, use `groovyCOnsole`, and add the `diozero-101-1.0-all.jar` to the classpath (Menu `Script` > `Add Jar(s) to ClassPath`), or
```
$ groovy -cp build/libs/diozero-101-1.0-all.jar src/main/groovy/DioZeroGroovy.groovy 
```
To run the above, make sure the `groovy` runtime version matches the one in `build.gradle`.

---
