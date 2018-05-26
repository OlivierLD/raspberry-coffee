## Comparing several programming languages

### The problem to solve
We want to solve a system of `n` equations, with `n` unknowns.

This problem implies recursive algorithms.
Object oriented languages are not a requirement.
Functionnal programming is not a requirement.

We will use the following programming languages:
- Java
- Scala
- Kotlin
- JavaScript
- Python

#### Output
All versions of the program pretty much return the same output, like
```
 Solving:
 (12.000000 x A) + (13.000000 x B) + (14.000000 x C) = 234.000000
 (1.345000 x A) + (-654.000000 x B) + (0.001000 x C) = 98.870000
 (23.090000 x A) + (5.300000 x B) + (-12.340000 x C) = 9.876000

 Done is 114,307 nano(Å) sec.
 x = 6.488222
 y = -0.137817
 z = 11.280925
```

#### Compilation
Java, Scala and Kotlin require a compilation.

Run
```
 $ ../gradlew shadowJar
```

#### Java
The sources are in `src/java/raspisamples/matrix`.

To run it:
```
 $ java -cp ./build/libs/RasPISamples-1.0-all.jar raspisamples.matrix.SystemUtil
```

#### Scala
The sources are in `src/scala/systems`

To run it:
```
 $ scala -cp ./build/libs/RasPISamples-1.0-all.jar systems.SystemUtils
```

#### Kotlin
The sources are in `src/kotlin/KtSystemSolver.kt`

```
 $ java -cp ./build/libs/RasPISamples-1.0-all.jar systemsKt.KtSystemSolverKt
```
or
```
 $ kotlin -classpath ./build/libs/RasPISamples-1.0-all.jar systemsKt.KtSystemSolverKt
```


#### JavaScript
See the sources - and more - in the `smoothing/js` folder, file `matrix.js`.

From `node.js`:
```
 $ cd smoothing/js
 $ node matrix.js from-node
```

Or ust load `smoothing.spray.html` in a browser.

#### Python
Sources are in `src/python/system.py`.

To run it:
```
 $ cd src/python
 $ python system.py
```
