# [DIOZERO](https://www.diozero.com/)

Another option than `PI4J`. Let's give it a try.  
Seems not to require any system library. Interesting.

- diozero [git repo](https://github.com/mattjlewis/diozero)
- Samples: <https://github.com/mattjlewis/diozero/tree/main/diozero-core/src/main/java/com/diozero/devices>

> We want to make sure this work on other JVM Languages too.
> Install `sdkman`: `curl -s get.sdkman.io | bash`  
> `$ source ~/.sdkman/bin/sdkman-init.sh`
> - Scala (install [Scala](https://sdkman.io/sdks#scala))
> - Kotlin (install [Kotlin](https://sdkman.io/sdks#kotlin))
> - Groovy (install [groovy](https://sdkman.io/sdks#groovy), or [here](https://groovy-lang.org/install.html))

Run this first sample, from the `diozero-101` folder:
```
$ ../gradlew shadowJar
$ java -jar build/libs/diozero-101-1.0-all.jar
```
For Kotlin:
```
$ kotlin -cp build/libs/diozero-101-1.0-all.jar diozerokt.FirstTestKt 
```
For Scala:
```
$ scala -cp build/libs/diozero-101-1.0-all.jar DioZero
```
For Groovy, use `groovyCOnsole`, and add the `diozero-101-1.0-all.jar` to the classpath.

---
