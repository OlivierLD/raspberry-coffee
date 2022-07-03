# [DIOZERO](https://www.diozero.com/)

Another option than `PI4J`. Let's give it a try.  
Seems not to require any system library. Interesting.

- diozero [git repo](https://github.com/mattjlewis/diozero)
- Samples: <https://github.com/mattjlewis/diozero/tree/main/diozero-core/src/main/java/com/diozero/devices>

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
