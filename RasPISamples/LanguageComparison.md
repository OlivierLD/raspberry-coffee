## Comparing several programming languages

This is just an example: for a given problem, provide a solution. Solutions will be implemented in different languages.

> _Note_: This document is a `KISS` document (Keep It Small and Stupid). We will _not_ provide here all you need to know to read it fluently.
> If - for example - you do not know what a compiler is, you will need to find it somewhere else.
> This being said, good luck!

### The problem to solve

> We want to solve a system of `n` equations, with `n` unknowns.
> The dimension `n` is dynamic, i.e. _not_ hard-coded,
> provided at runtime.

This is - in my opinion - the kind of problems that do reveal a lot of the features of a language.
This problem implies recursive algorithms, declaring functions, functions admitting arrays (of arrays...) of parameters, etc.
In the context of this document:
- Being Object Oriented (OO) is not a requirement.
- Functionnal Programming is not a requirement.

The idea here is to be able to compare the different source codes corresponding to each language, addressing the exact same problem,
and - hopefully - returning the same result.

Comparing the length of the code, its readability, its complexity, its structure, and possibily _its beauty_
can be an interesting journey.

This document certainly does not pretend to be exhausive. There are many languages available, I have used several of them,
but we will not talk about all of those. BASIC, COBOL, FORTRAN, Pascal, etc, are not part of this picture.

This does not mean that those languages are not interesting, this only means that this document is not an anthology of the Computer literacy.

> One of those languages though, could deserve some interest, [brainfuck](https://en.wikipedia.org/wiki/Brainfuck). It is a complete
Turing machine, using only 8 instructions. This being said, let's move on.

## System Resolution

We will use the following programming languages:
- [Java](#java)
    - Created by James Gosling, at Sun, in 1995.
    - Uses a Java Virtual Machine (JVM). 100% Object Oriented, Functional features since Java 8. Requires compilation (`javac`). Strongly typed.
    The implementation the JVM is the key to portability. Java motto used to be _write once, run everywhere_. And this is indeed true,
    this **does** work, from single-board computers like the Raspberry PI Zero to big clusters of main-frames. Yes.
- [Scala](#scala)
    - Created by Martin Odersky, École Polytechnique Fédérale de Lausanne, in 2003.
    - Uses the _same_ Virtual Machine as Java (and thus gets Java protability for free), Object Oriented _and_ Functional since day one. Extremely well designed, natively implements immutability and other key concepts.
    Can sound a bit complex to a beginner, but addicting after that.
    Requires compilation (`scalac`).
- [Kotlin](#kotlin)
    - Created by the JetBrains team (author of the IntelliJ IDE, the best), in 2011.
    - Somewhat similar to `Scala`, requires compilation (`kotlinc`).
- [JavaScript](#javascript)
    - Created in 1995, _in 10 days_ by Brendan Eich, engineer at Netscape.
    - Initially designed to run in a browser. Not typed (on purpose), and natively functional. Regained _a lot_ of popularity when Web 2.0 was released, used everywhere a browser runs, to program client-side applications (a **lot** of JavaScript frameworks are available).
    Interestingly, some server-side code can also be written using JavaScript, since the success of [`nodejs`](https://nodejs.org/).
    `NodeJS` is a JavaScript runtime built on `V8`, Chrome's JavsScript engine, open source.
    This is what makes JavaScript one of the very few languages that can be used on the client as well as on the server.
    Interpreted, does not require compilation.
- [Python](#python)
    - Created by Guido van Rossum, in 1991.
    - Designed for educational purpose. Easy to use, even if its syntax and structure might not please eveyone.
    No native Object Oriented (OO) features (some exist, but they may look weird to an OO adict).
    Does not require compilation. Very popular among data scientists, mostly due to its simlicity.
- [C](#c)
    - Created by Dennis Ritchie, between 1969 and 1973, at Bell Labs.
    - Immensly adopted on the planet Earth. The oldest of all the others, and still alive and well.
    `C` has no OO features (it avatar `C++` does have some). No Functional Programming features.
    Requires explicit memory allocation and de-allocation (`alloc` and `free`), extensively uses _pointers_,
    which is what leads to vast majority of the bugs `C` developers have to deal with. Requires compilation (`gcc`, `cc`, etc, the compilator depends on the machine you are on).
    Running the same `C` program on different Operating Systems often - if not always - requires a re-compilation of the code.
    The code is compiled natively - hence does not require a Virtual Machine. These two aspects (lack of portability, having to deal with pointers)
    are two of the most important ideas that gave birth to Java (Java has no pointers, and once compiled runs everywhere there is a JVM. See above).
    - The book `The C Programming Language` by Brian Kernighan and Dennis Ritchie has been the
    bible of several generations of programmers.
- [Groovy](#groovy)
    - Created by James Strachan, in 2004.
    - Runs on a JVM (see above). Much more flexible language than Java, _not_ strongly typed, compatible with Java (i.e you
    can litterally copy-paste Java code into a Groovy script), but also understands a nice closure-like syntax.
    Can be compiled, or not. Supports classes definition, can be run as a script.

TODO
- Go
    - Created in 2009 by Robert Griesemer, Rob Pike, and Ken Thompson, at Google.
- Clojure
    - Created by Rich Hickey, in 2007.
- and more...

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

#### A quick note
The code presented here can certainly be optimized, tweaked, or made nicer, there is no question left about that.
But it certainly gives a taste of what the different languages can feel like. If anyone has ideas to make the code look nicer, please do use the [`issues section`](../../../issues)
available in `git`.

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

#### Groovy
Sources are in [`scr/groovy/system.groovy`](./src/groovy).

To run it, after installing groovy on your machine:
```
 $ cd src/groovy
 $ groovy system.groovy
```
or just
```
 $ groovy system
```

### A bit of history, to predict the future
In the scope we are considering here, the first to emerge was `C`. It is the `native` language of Unix,
the Unix operating system is 100% written in C. The sources of Unix were (are) available on the system, this was early open source.

It later gave birth to Linux.

This openness of Unix was probably one of the reasons of the success of `C` - beside its efficiency.

Developpers working with `C` had to face two major issues:
###### Portability
To make a C program run on several systems, you had to re-compile the code on the target system, and you had to tweak the code to fit some system aspects, like endianness, word sizes, and similar features.
This was implemented using `#define` and `#ifdef` statements in the code, pre-processed before compilation, so the code matches the requirement of the compiler. Like
```C
 #define VAX_VMS

 #ifdef VAX_VMS
   // do this for the VAX
 #endif
 #ifdef WINDOWS
   // do that for Windows
 #endif
   // ... etc
```
All the `#define` statements very often make the code difficult to read and maintain.
###### Memory management
`C` is extensively using pointers. You have to allocate memory to a pointer in order to use it, and free
it afterwards, for the memory to be released, in order to be re-used.
More than half the bugs `C` programmers have to deal with are usually pointer-related.
Also, threads (concurrent programming) management in `C` is not a nightmare. It's worse.

This is what paved to way for Java.

Java is using a syntax similar to C. It used a similar way to declare function and methods, uses curly braces `{}` to define code blocks, *but*:
- There is no `#define` statement
- It does not require the programmer to deal with pointers
- It runs on a Java Virtual Machine. As a result, a `class` compiled somewhere will run
_without any modification_ on any other system where a JVM is available. The portability is taken care of by the
implementation of the virtual machine.
- Java does not need linkage (link libraries),  the `classpath` takes care of it.

This eliminates a lot of the issues inherent to `C`.

The Virtual Machine that runs your java code (compiled into `class`es) has 3 threads:
- A runtime - responsible for running the code
- A finalizer - responsible for flagging any object no one is pointing to any more
- A garbage collector - responsible for freeing the memory used by the finalized objects.

If you can generate a `class`, running it will come for free if you can find a JVM.
This is where other JVM-compatible languages emerged.

Scala for example, "only" had to create a compiler that turns Scla code into a `class` - just like the `javac` compiler does for Java code.
Then the class is run by the JVM, that does not even need to know what language this class was
originally written in.

There are many such languages, `Scala`, `Kotlin`, `Groovy`, `Clojure` being only a few of them.

Then, a un-anticipated shift happened: the emergence of the Containers, like `Docker`.
Docker is generating system images. It provides an environment - an operating system - onto
which you can run the programs you need.

In other words, if your program runs on a given Docker image, you will _not_ give the compiled code to whoever
wants to run it somewhere else, you will provide a copy of the full Docker image. And whoever will
run it will feel like you did on yours. This is not portability anymore, this is virtualization.

`Go` for example, does not care about portability. It can rely on Docker. Virtually.

### So what?
So yes, what's the point here?

Computers' brain is the processor that was plugged in it, and this guy only understands binary code, right?

Right. But here is the thing:
- Any idiot can write a program a computer will understand. The real challenge is to write programs a _human_ can understand.

And if I may add: a human might be the guy who fixes the bugs (they happens) in your code. Maintainability might be one of the reasons why all those languages exist.

And they _are_ real languages. Human languages usually go two ways. You talk, someone talks back.

With computer languages, you talk, it works or fails (for now).

But still, they _are_ languages.
They have rules, vocabulary, syntax, grammar, and even styles. In the team(s) I work in, I can tell by watching the code who wrote it.
And I am not - by very far - the only one.

------------------------------
Oliv fecit, A.D. 2018.
