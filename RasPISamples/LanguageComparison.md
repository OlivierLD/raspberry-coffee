## Comparing several programming languages

### The problem to solve
We want to solve a system of `n` equations, with `n` unknowns. The dimension `n` is dynamic, i.e. _not_ hard-coded,
provided at runtime.

This problem implies recursive algorithms.
Object oriented languages are not a requirement.
Functionnal programming is not a requirement.

The idea here is to be able to compare the codes corresponding to each language, addressing the exact same problem,
and - hopefully - returning the same result.

Comparing the length of the code, its readability, its complexity, its structure, and possibily its beauty
can be an interesting exercise.

We will use the following programming languages:
- [Java](#java)
    - Uses a Virtual Machine (JVM). Object Oriented, Functional features since Java 8. Requires compilation (`javac`). Strongly typed.
- [Scala](#scala)
    - Uses the _same_ Virtual Machine as Java, Object Oroientd _and_ Functional since day one. Extremely well designed, natively implements immutability and other key concepts.
    Can sound a bit complex to a beginner, but addicting after that.
    Requires compilation (`scalac`).
- [Kotlin](#kotlin)
    - Somewhat similar to `Scala`, designed by the JetBrain team (author of the IntelliJ IDE, the best).
    Requires compilation (`kotlinc`).
- [JavaScript](#javascript)
    - Has been around for years. Designed _in 10 days_ by a Netscape engineer.
    Not typed (on purpose), and natively functional. Regained _a lot_ of popularity when Web 2.0 was released, used everywhere a browser runs, to program client-side application (a **lot** of JavaScript frameworks are available). Interestingly, some servers can also be programmed using JavaScript, since the success of [`nodejs`](https://nodejs.org/). `NodeJS` if a JavaScript runtime built on `V8`, Chrome's JavsSCript engine, open source.
    This is what makes JavaScript one of the very few languages that can be used on the cient as well as on the server.
    Interpreted, does not require compilation.
- [Python](#python)
    - Designed for educational purpose. Easy to use, even if its syntax and structure might not please eveyone.
    No native Object Oriented (OO) features (some exist, but they may look weird to an OO adict).
    Does not require compilation. Very popular among data scientist, mostly due to its simlicity.
- [C](#c)
    - Immensly adopted on the planet Earth. The oldest of all the others, and still alive and well.
    `C` has no OO features (it avatar `C++` does have some). No Functional Programming features.
    Requires explicit memory allocation and de-allocation (`alloc` and `free`), extensively uses _pointers_,
    that lead to vast majority of the bugs `C` developers have to deal with. Requires compilation (`gcc`, `cc`, etc, the compilator depends on the machine you are on).
    Running the same `C` program on different Operating Systems often - if not always - requires a re-compilation of the code.
    The code is compiled natively - hence does not require a Virtual Machine. These two aspects (lack of portability, having to deal with pointers)
    are two of the most important ideas that gave birth to Java (Java has no pointers, and once compiled runs everywhere there is a JVM. The early moto of Java was
    _write once, run everywhere_).

TODO
- Groovy
- Go

#### Output
All versions of the program pretty much return the same output, like
```
 Solving:
 (12.000000 x A) + (13.000000 x B) + (14.000000 x C) = 234.000000
 (1.345000 x A) + (-654.000000 x B) + (0.001000 x C) = 98.870000
 (23.090000 x A) + (5.300000 x B) + (-12.340000 x C) = 9.876000

 Done in 114,307 Ås (nano).
 A = 6.488222
 B = -0.137817
 C = 11.280925
```

#### Compilation
Java, Scala and Kotlin require a compilation. C too, but not from Gradle.

Run
```
 $ ../gradlew shadowJar
```

#### Java
The sources are in [`src/java/raspisamples/matrix`](./src/java/raspisamples/matrix).
(See also [this document](./src/java/raspisamples/smoothing/README.md) ).

To run it:
```
 $ java -cp ./build/libs/RasPISamples-1.0-all.jar raspisamples.matrix.SystemUtil
```

#### Scala
The sources are in [`src/scala/systems`](./src/scala/systems).

To run it:
```
 $ scala -cp ./build/libs/RasPISamples-1.0-all.jar systems.SystemUtils
```

#### Kotlin
The sources are in [`src/kotlin/KtSystemSolver.kt`](./src/kotlin).

```
 $ java -cp ./build/libs/RasPISamples-1.0-all.jar systemsKt.KtSystemSolverKt
```
or
```
 $ kotlin -classpath ./build/libs/RasPISamples-1.0-all.jar systemsKt.KtSystemSolverKt
```


#### JavaScript
See the sources - and more - in the [`smoothing/js`](./smoothing/js) folder, file `matrix.js`.

From `node.js`:
```
 $ cd smoothing/js
 $ node matrix.js from-node
```

Or just load `smoothing.spray.html` in a browser.

#### Python
Sources are in [`src/python/system.py`](./src/python).

To run it:
```
 $ cd src/python
 $ python system.py
```

#### C
Sources are in [`src/C/system.c`](./src/C).

To compile and run (the compilation is not done by Gradle, and may vary depending on your system):
```
 $ cd src/C
 $ gcc -lm -o system system.c
 $ ./system
```

---
Oliv fecit, A.D. 2018.
