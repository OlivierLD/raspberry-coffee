## Comparing several programming languages

This is just an example: for a given problem, provide a solution. Solutions will be implemented in different languages.

> _Note_: This document is a `KISS` document (Keep It Small and Stupid (some say Simple)). We will _not_ provide here all you need to know, to read it fluently.  
> If - for example - you do not know what a compiler is, you will need to find it somewhere else.
> This being said, good luck!

### The problem to solve

> We want to solve a system of `n` equations, with `n` unknowns.
> The dimension `n` is dynamic, i.e. _not_ hard-coded,
> provided at runtime.

For example, `n` = 3, and given the system:
```
  /
  |   (12 * X)    + (13 * Y)   + (14 * Z)     = 234
 <    (1.345 * X) + (-654 * Y) + (0.001 * Z)  = 98.87
  |   (23.09 * X) + (5.3 * Y)  + (-12.34 * Z) = 9.876
  \
```
<!-- -->
$$
\begin{math}
\Bigl\{
\begin{array}{l}
(12 * X)    + (13 * Y)   + (14 * Z)     = 234   \\
(1.345 * X) + (-654 * Y) + (0.001 * Z)  = 98.87 \\
(23.09 * X) + (5.3 * Y)  + (-12.34 * Z) = 9.876
\end{array}
\end{math}
$$
<!-- -->

The question is: _What are the values of X, Y, and Z?_

This is - in my opinion - the kind of problems that do reveal a lot of the features of a language.
This problem implies recursive algorithms, declaring functions, functions admitting arrays (of arrays...) of parameters, etc.
In the context of this document:
- Being Object Oriented (OO) is not a requirement.
- Functional Programming is not a requirement.

The idea here is to be able to compare the different source codes corresponding to each language, addressing the exact same problem,
and - hopefully - returning the same result.

Comparing the length of the code, its readability, its complexity, its structure, and possibly _its beauty_
can be an interesting journey.

This document certainly does not pretend to be exhaustive. There are [many languages](https://en.wikipedia.org/wiki/Category:Lists_of_programming_languages) available, I have used several of them,
but we will not talk about all of those. BASIC, COBOL, FORTRAN, Pascal, etc, are not part of this picture.

This does not mean that those languages are not interesting, this only means that this document is not an anthology of the Computer literacy.

See languages popularity on [Tiobe](https://www.tiobe.com/tiobe-index/).

> One of those languages though, could deserve some interest, [brainfuck](https://en.wikipedia.org/wiki/Brainfuck). It is a complete
Turing machine, _using only 8 instructions_. This being said, let's move on.

## System Resolution

We will use the following programming languages:
- [Java](#java)
    - Created by James Gosling, at Sun, in 1995.
    - Uses a Java Virtual Machine (JVM). 100% Object Oriented, Functional features since Java 8 (to catch up with Scala, see below). Requires compilation (`javac`). Strongly typed.
    The implementation the JVM is the key to portability. Java motto used to be _write once, run everywhere_. And this is indeed true,
    this **does** work, from single-board computers like the Raspberry Pi Zero to big clusters of main-frames. Yes.
    - Java is not the first language to introduce the concept of Virtual Machine (at least ADA did it before, named after... guess who?). But Java _specified_ it.
    This specification also includes the Java Platform Debugging Architecture (JPDA), that is a blessing for developers. _You can debug - including remotely - the same way on all systems_.
    No language - to my knowledge - ever did that before.
    - Since version 9, Java now has a `REPL` (Read-Evaluate-Print-Loop). Type `jshell` to launch it.
- [Scala](#scala)
    - Created by Martin Odersky, École Polytechnique Fédérale de Lausanne, in 2003.
    - Uses the _same_ Virtual Machine as Java (and thus gets Java portability for free), Object Oriented _and_ Functional since day one. Extremely well designed, natively implements immutability and other key concepts.
    Can sound a bit complex to a beginner, but addicting after that.
    Requires compilation (`scalac`).
    - Has a `REPL`. Type `scala` to launch it.
- [Kotlin](#kotlin)
    - Created by the JetBrains team (author of the IntelliJ IDE, the best), in 2011.
    - Uses JVM at run time.
    - Somewhat similar to `Scala`, requires compilation (`kotlinc`).
        - Kotlin's syntax is simpler than Scala's one. Kotlin seems to gain more and more popularity, as big guys like Google now support it.
    - Has a `REPL`. Type `kotlinc` to launch it.
    - See [this](https://kotlinlang.org/docs/tutorials/command-line.html).
- [JavaScript](#javascript)
    - Created in 1995 by Brendan Eich, engineer at Netscape, _in 10 days_!!
    - Initially designed to run in a browser. Loosely typed (on purpose), and natively functional. Regained _a lot_ of popularity when Web 2.0 was released, used everywhere a browser runs, to program client-side applications (a **lot** of JavaScript frameworks are available).
    Interestingly, some server-side code can also be written using JavaScript, since the successful release of [`nodejs`](https://nodejs.org/).
    `Node.js` is a JavaScript runtime built on `V8`, Chrome's JavaScript engine, open source.
    This is what makes JavaScript one of the very few languages that can be used on the client as well as on the server.
    Interpreted, does not require compilation.
    - `Node.js` can be used as a `REPL`. Type `node` to launch it.
- [Python](#python)
    - Created by Guido van Rossum, in 1991.
    - Designed for educational purpose. Easy to use, even if its syntax and structure might not please everyone.
    No native Object Oriented (OO) features (some exist, but they may look weird to an OO addict).
    Does not require compilation. Very popular among data scientists, mostly due to its simplicity.
    - Has a `REPL`. Type `python` or `python3` to launch it.
- [C](#c)
    - Created by Dennis Ritchie, between 1969 and 1973, at Bell Labs.
    - Immensely adopted on the planet Earth. The oldest of all the ones mentioned here, and still alive and well.
    `C` has no OO features (its avatar `C++` does have some). No Functional Programming features.
    Requires explicit memory allocation and de-allocation (`alloc` and `free`), extensively uses _pointers_,
    which is what leads to vast majority of the bugs `C` developers have to deal with. Requires compilation (`gcc`, `cc`, etc, the compiler depends on the machine you are on).
    Running the same `C` program on different Operating Systems often - if not always - requires a re-compilation of the code.
    The code is compiled natively - hence does not require a Virtual Machine. These two aspects (lack of portability, having to deal with pointers)
    are two of the most important ideas that gave birth to Java (Java has no pointers, and once compiled runs everywhere there is a JVM. See above).
    - The book `The C Programming Language` by Brian Kernighan and Dennis Ritchie has already been the
    bible of several generations of programmers.
    > Whoever has ever tried to learn a new language has probably faced an example
    > printing something like "`hello, world`" (or "`hello something`"). It comes from this book; page 5 in mine. ;)
- [PHP](#php)
    - Created in 1994 by Rasmus Lerdorf.
    - The very first incarnation of PHP was a simple set of Common Gateway Interface (CGI) binaries written in the C programming language.
    - It is now widely adopted by many web sites.
- [Groovy](#groovy)
    - Created by James Strachan, in 2004.
    - Runs on a JVM (see above). Much more flexible language than Java, _not_ strongly typed, compatible with Java (i.e you
    can literally copy-paste Java code into a Groovy script), but also understands a nice closure-like syntax.
    Can be compiled, or not. Supports classes definition, can be run as a script.
    - The `Groovy Console` can be used as a `REPL`. Type `groovysh` to launch it.
- [Ruby](#ruby)
    - Ruby is a dynamic, interpreted, reflective, object-oriented, general-purpose programming language.
    - It was designed and developed in the mid-1990s by Yukihiro "Matz" Matsumoto in Japan.
    - It has an interactive console. Type `irb` to launch it.
- [Go](#go) (aka Golang)
    - Created in 2009 by Robert Griesemer, Rob Pike, and Ken Thompson, at Google.
    - Designed to be the "Language of the Cloud". Interpreted (`go run`) or compiled (`go build`).
    Looks somewhat like `C` (Ken Thompson was part of Bell Labs,
    and worked with Dennis Ritchie and Brian Kernighan). _Uses pointers!_
    Feels a bit like an UFO here, but very fast and powerful.
    The `go build` command can build a **native executable**, that does not need a Virtual Machine.
    Portability is to be provided by Containers (like Docker).
- [Clojure](#clojure)
    - Created by Rich Hickey, in 2007.
    - Clojure is a JVM-compatible LISP-like language. LISP is the ancestor of all Functional Programming languages.
    If you do not know it yet, do give it a look. There is a good chance that you
    will be at least surprised... If Go feels a bit like an UFO, that one is an alien!
    Clojure is _obsessed_ with immutability. You just _cannot_ assign a new value to a variable. And yes, that can make sense.
    - Has a `REPL`. Type `clj` to launch it.
- [Processing](#processing)
    - Created by Ben Fry and Casey Reas, based on Java (not exactly _another_ language in that sense, but it *does* make things a lot simpler), in 2001. Beautiful. Available (for free) from the [Processing](https://processing.org/) site.
    - It is what inspired the `Arduino` IDE - which uses a language that looks more like `C`.
    - Amazing graphical capabilities (but not only), with a simple user interface, easy to access. I like it.<br/>
    _A personal note:_ Java code through Processing seems easier (to me) than Python.
    Ramping up from Processing to Java is a natural and smooth move. Ramping up from Python to anything else is not...
    > _Note:_ Today (June 2018), Processing does not support (yet) Java 8 features (like lambdas, stream apis, etc).
- [Mathematica](#mathematica)
    - [Mathematica](https://www.wolfram.com/mathematica/) comes _for free_ on the Raspberry Pi.
    - Not exactly a language, more like a computing environment, but definitely worth a look.
    - Created by Stephen Wolfram, starting in 1986.
    - The system above can be resolved very quickly in just a couple of lines:

    ![Mathematica](./img/Mathematica.png)

    <!--iframe width='800' height='400' src='https://www.wolframcloud.com/obj/olivier3/Published/system.nb?_view=EMBED' frameborder='0'></iframe-->
 - [R](#R)
    - More like a scripting than programming language, but has good reasons to exist! See the code...
    - Definitely designed for mathematicians and statisticians (see [here](https://en.wikipedia.org/wiki/R_(programming_language)))
    - The system above can be resolved very quickly in just a couple of lines of code.

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
 $ ../../gradlew shadowJar [ -x:Project.Trunk:System-Languages:compileClojure ]
```

#### A quick note
The code presented here can certainly be optimized, tweaked, or made nicer, there is no question left about that.
But it certainly gives a taste of what the different languages can feel like. If anyone has ideas to make the code look nicer, please do use the [`issues section`](../../../issues)
available in `git`.

#### Java
The sources are in [`src/main/java/matrix`](./src/main/java/matrix).
(See also [this document](./src/main/java/smoothing/README.md) ).

To run it:
```
 $ java -cp ./build/libs/System-Languages-1.0-all.jarr matrix.SystemUtil
```

#### Scala
The sources are in [`src/main/scala/systems`](./src/main/scala/systems).

To run it:
```
 $ scala -cp ./build/libs/System-Languages-1.0-all.jar systems.SystemUtils
```

#### Kotlin
The sources are in [`src/main/kotlin/KtSystemSolver.kt`](./src/main/kotlin).

```
 $ java -cp ./build/libs/System-Languages-1.0-all.jar systemsKt.KtSystemSolverKt
```
or
```
 $ kotlin -classpath ./build/libs/System-Languages-1.0-all.jar systemsKt.KtSystemSolverKt
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
Sources are in [`src/main/python/system.py`](./src/main/python).

To run it:
```
 $ cd src/main/python
 $ python system.py
```

> Note: there is or Python a popular numerical library called `numpy`. We present here an
> example using is, just run
```
 $ python system.np.py
```
> It makes the code much simpler.

#### C
Sources are in [`src/main/C/system.c`](./src/main/C).

To compile and run (the compilation is _not_ done by Gradle, and may vary depending on your system):
```
 $ cd src/main/C
 $ gcc -lm -o system system.c
 $ ./system
```

On the Raspberry Pi, use:
```
 $ cd src/main/C
 $ g++ -Wall -o system system.c
 $ ./system
```

#### PHP
Sources are in [`src/main/php/`](./src/main/php). Thanks to Sébastien Morel for tuning up the `php` code.

See instructions at [src/php/README.md](src/main/php/README.md).

#### Groovy
Sources are in [`scr/main/groovy/system.groovy`](./src/main/groovy).

To run it, after installing Groovy on your machine:
```
 $ cd src/main/groovy
 $ groovy system.groovy
```
or just
```
 $ groovy system
```

#### Ruby
Sources are in [`src/main/ruby/matrix.rb`](./src/main/ruby)

To run the system resolution, type
```
 $ cd src/main/ruby
 $ irb matrix.rb
```
or without the REPL:
```
 $ cd src/main/ruby
 $ ruby matrix.rb
```
#### Go
Sources are in [`src/main/go/system.go`](./src/main/go).

To run it - after you've [installed Go](https://golang.org/doc/install) on your machine:
```
 $ cd src/main/go
 $ go run system.go
```
It can be built (compiled) into a native executable:
```
 $ go build system.go
 $ ./system
```

#### Clojure
> This is a Work In Progress, I'm learning (I should have picked up Chinese, that would have been simpler).

Sources are (will be) in [`src/main/clojure`](./src/main/clojure).

To run it, after [installing Clojure](http://clojure.org) on your machine:
```
 $ cd src/main/clojure
 $ CLOJURE_JAR=[wherever-you-put-it]/clojure-tools-1.9.0.381.jar
 $ CP=.:$CLOJURE_JAR
 $ java -cp .:$CP clojure.main --main systems.matrix

```

#### Processing
Sources are in [`src/main/Processing`](./src/main/Processing).

It is an interactive curve resolution, using the `least squares` method.

<img src="./img/least.square.png" width="600" height="619" alt="Processing" style="text-align: center;">

If needed, change the degree of the result polynomial using the slider at the bottom of the screen. You can also use the left and right arrows of the keyboard.

See the calculated coefficients in the console output.

Find [here](https://github.com/OlivierLD/raspberry-coffee/tree/master/Processing) a bit more about Processing on the Raspberry Pi.

You can generate an executable from your Processing sketch.
Open your sketch in Processing (here `System.pde`), then go to `File` > `Export Application`.
Then choose your OS, and click `Export`. Then you have an executable ready to go!

#### Mathematica
Look at that:
- 1. Define the procedure (called a `Module`):
```
In[23]:= system[matrix_, coeff_] :=
     Module[{solution}, solution = Inverse[matrix] . coeff]
```
This defines a function taking a matrix and a vector as parameters.
- 2. Now, it can be invoked:
```
In[24]:= system[
   {{12, 13, 14},
     {1.345, -654, 0.001},
     {23.09, 5.3, -12.34}},
   { 234, 98.87, 9.876 }
 ]
```
- 3. And the result is
```
Out[24]= {6.48822, -0.137817, 11.2809}
```
Again, Mathematica is not a programming language. But as it is able to provide the answer to our problem
in so few lines..., it had to be mentioned here.

#### R
Sources are in [`src/main/R`](./src/main/R).
- Install R: <https://courses.edx.org/courses/UTAustinX/UT.7.01x/3T2014/56c5437b88fa43cf828bff5371c6a924/>
- R-Studio: <https://rstudio.com/products/rstudio/download/#download>
- Good resource to get started at <https://www.geeksforgeeks.org/matrix-multiplication-in-r/?ref=lbp>

To start the R REPL:
```
$ R

R version 4.0.2 (2020-06-22) -- "Taking Off Again"
Copyright (C) 2020 The R Foundation for Statistical Computing
Platform: x86_64-apple-darwin17.0 (64-bit)

R is free software and comes with ABSOLUTELY NO WARRANTY.
You are welcome to redistribute it under certain conditions.
Type 'license()' or 'licence()' for distribution details.

  Natural language support but running in an English locale

R is a collaborative project with many contributors.
Type 'contributors()' for more information and
'citation()' on how to cite R or R packages in publications.

Type 'demo()' for some demos, 'help()' for on-line help, or
'help.start()' for an HTML browser interface to help.
Type 'q()' to quit R.

>
```

To run the system resolution, use `Rscript`:
```
src/main/R $ Rscript system.R
     [,1]   [,2]    [,3]
l1 12.000   13.0  14.000
l2  1.345 -654.0   0.001
l3 23.090    5.3 -12.340
[1]  6.4882219 -0.1378166 11.2809252
```

### A bit of history, to predict the future
In the scope we are considering here, the first to emerge was `C`. It is the `native` language of Unix,
the Unix operating system is 100% written in C. The sources of Unix were (are) available on the system, this was early open source.

It later gave birth to Linux.

This openness of Unix was probably one of the reasons of the success of `C` - beside its efficiency.

Developers working with `C` had to face two major issues:
###### Portability
To make a C program run on several systems, you had to re-compile the code on the target system, and you had to tweak the code to fit some system aspects, like endianness, word sizes, and similar features.
This was implemented using `#define` and `#ifdef` statements in the code, pre-processed before compilation, so the code matches the requirements of the compiler. Like
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
All the `#define` statements (aka `compilation options`) very often make the code difficult to read and maintain.
###### Memory management
`C` is extensively using pointers. _You have to allocate memory_ to a pointer in order to use it, and free
it afterwards, for the memory to be released, in order to be re-used.
More than half the bugs `C` programmers have to deal with are usually pointer-related.
Also, threads (concurrent programming) management in `C` is not a nightmare. It's worse.

This is what paved to way for Java.

Java is using a syntax similar to C. It uses a similar way to declare functions and methods, uses curly braces `{}` to define code blocks, *but*:
- There is no `#define` statement
- It does not require the programmer to deal with pointers (it introduces the concept of Garbage Collector (GC))
- It runs on a Java Virtual Machine. As a result, a `class` compiled somewhere will run
_without any modification_ on any other system where a JVM is available. The portability is taken care of by the
implementation of the virtual machine.
    - The Java Virtual Machine (JVM) runs three threads:
        - The `runtime`, that runs your programs
        - The `finalizer`, that flags the variables no longer in use, so they can be freed
        - The `garbage collector` (GC) that removes (frees) the variables flagged by the `finalizer`
- Java does not need linkage (link libraries),  the `classpath` takes care of it.

This eliminates a lot of the issues inherent to `C`.

The Virtual Machine that runs your java code (compiled into `class`es) has 3 threads:
- A runtime - responsible for running the code
- A finalizer - responsible for flagging any object no one is pointing to any more
- A garbage collector - responsible for freeing the memory used by the finalized objects.

If you can generate a `class`, running it will come for free if you can find a JVM.
This is where other JVM-compatible languages emerged.

> _Note_: The JVM actually understands some sort of code known as `byte code`, produced by the compiler for the JVM.
> As you can tell, it looks nothing like Java (nor anything else).
>
> To see what the `byte code` looks like, use the `javap` utility of the SDK:
```
 $ javap -cp build/libs/RasPISamples-1.0-all.jar -c raspisamples.matrix.SystemUtil
   Compiled from "SystemUtil.java"
   public class raspisamples.matrix.SystemUtil {
     public raspisamples.matrix.SystemUtil();
       Code:
          0: aload_0
          1: invokespecial #1                  // Method java/lang/Object."<init>":()V
          4: return

     public static double[] solveSystem(double[], double[]);
       Code:
          0: new           #2                  // class raspisamples/matrix/SquareMatrix
          3: dup
          4: aload_1
          5: arraylength
          6: invokespecial #3                  // Method raspisamples/matrix/SquareMatrix."<init>":(I)V
          9: astore_2
         10: iconst_0
         11: istore_3
         12: iload_3
         13: aload_1
         14: arraylength
         15: if_icmpge     56
         18: iconst_0
         19: istore        4
         21: iload         4
         23: aload_1
         24: arraylength
         25: if_icmpge     50
         28: aload_2
         29: iload_3
         30: iload         4
         32: aload_0
         33: aload_1
         34: arraylength
...
        621: anewarray     #12                 // class java/lang/Object
        624: dup
        625: iconst_0
        626: aload         5
        628: iconst_2
        629: daload
        630: invokestatic  #16                 // Method java/lang/Double.valueOf:(D)Ljava/lang/Double;
        633: aastore
        634: invokestatic  #19                 // Method java/lang/String.format:(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
        637: invokevirtual #23                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
        640: return
}
```

Scala for example, "only" had to create a compiler that turns Scala code into a `class` (containing `byte code`) - just like the `javac` compiler does for Java code.
Then the class is run by the JVM, that does not even need to know what language this class was
originally written in.

There are many such languages, `Scala`, `Kotlin`, `Groovy`, `Clojure` being only a few of them.

More technical details are available from the module [Other JVM languages](https://github.com/OlivierLD/raspberry-coffee/tree/master/OtherJVM.languages), in this project.

Then, an un-anticipated shift happened: the emergence of the Containers, like `Docker`.
Docker is generating system images. It provides an environment - an operating system - onto
which you can run the programs you need.

And communication between machines is less and less a problem (this has not always been the case... Back in the days, Microsoft and Apple had different ways to format their floppies,
making them un-readable by another OS). REST, HTTP, TCP, etc, are - so far - OS agnostic.

In other words, if your program runs on a given Docker image, you will _not_ give the compiled code to whoever
wants to run it somewhere else, you will provide a copy of the full Docker image. And whoever will
run it will feel like you did on yours. This is not portability anymore, this is virtualization.

`Go` for example, does not care much about portability. It can rely on Docker. Virtually.

### So what?
So yes, what's the point here?

Computers' brain is the processor that was plugged in it, and this guy only understands binary code, right?

Right. But here is the thing:
- Any idiot can write a program a computer will understand. The real challenge is to write programs a _human_ can understand.

And if I may add: a human might be the guy who fixes the bugs (they happens) in your code. Maintainability might be one of the reasons why all those languages exist.
Code needs to be readable, and understandable by anyone, specially if the guy who wrote it in the first place does not
work here any more.

Those languages _are real languages_. Human languages usually go two ways. You talk, someone talks back.

With computer languages, you talk, it works or fails (for now).

But still, they _are_ languages.
They have rules, vocabulary, syntax, grammar, and even styles. In the team(s) I work in, I can tell by reading the code who wrote it.
And I am not - by very far - the only one.

#### Portability ?
Let's forget about Virtual Machines for a moment.
Let's think about browsers...

The same JavaScript code is supposed to run the same in all browsers, right?
It's like if the browser was playing the role of a Virtual Machine, taking care of rendering the same result,
wherever you run the code from.

Well, this would be in an ideal world... The specification of JavaScript is way more laxist than the JVM's one,
I am not even sure there was one before JavaScript was released for the first time.
Several ones came after that: ECMA Script, ES5, ES6... And as if it was not enough, the implementation of CSS (Cascading Style Sheets)
is following a similar pattern, it depends quite a bit on the browser you want to use, they all have their sensibility (IE, Edge, Chrome,
Opera, Firefox, Safari, you name it) !

This means that there is still a lot of room for inventions!

#### Docker
Installing Go on the Raspberry Pi happens not to be as straightforward as anyone would expect... It would be worth giving Docker a try.

This project contains a module that build Docker images, including one for Go.

See [here](../../../../tree/master/docker).

From the `docker` directory, just run
```
 $ ./image.builder.sh
 +-------------- D O C K E R   I M A G E   B U I L D E R --------------+
 | 1. Nav Server, Debian                                               |
 | 2. Web Components, Debian                                           |
 | 3. To run on a Raspberry Pi, Java, Raspberry Coffee, Web Components |
 | 4. Node PI, to run on a Raspberry Pi                                |
 | 5. Node PI, to run on Debian                                        |
 | 6. GPS-mux, to run on a Raspberry Pi (logger)                       |
 | 7. Golang, basics                                                   |
 | 8. Raspberry Pi, MATE, with java, node, web comps, VNC              |
 +---------------------------------------------------------------------+
 | Q. Oops, nothing, thanks, let me out.                               |
 +---------------------------------------------------------------------+
 == You choose =>
```
and choose option `7`.

This will build a Docker image with `Go` installed on it, and the `system.go` code will be copied in the `go/src/app` directory.
And it runs fine on a Raspberry Pi.

Then you can connect to the Docker image and run the `./app` executable to start the system resolution:
```
 $ docker run -it oliv-go:latest /bin/bash
 #####                                            ###
#     #   ####   #         ##    #    #   ####    ###
#        #    #  #        #  #   ##   #  #    #   ###
#  ####  #    #  #       #    #  # #  #  #         #
#     #  #    #  #       ######  #  # #  #  ###
#     #  #    #  #       #    #  #   ##  #    #   ###
 #####    ####   ######  #    #  #    #   ####    ###

root@483d4af443bc:/go/src/app# ./app
Resolving:
(12 x A) + (13 x B) + (14 x C) = 234
(1.345 x A) + (-654 x B) + (0.001 x C) = 98.87
(23.09 x A) + (5.3 x B) + (-12.34 x C) = 9.876
A = 6.48822194633027
B = -0.13781660635627724
C = 11.280925180476313
root@483d4af443bc:/go/src/app#
```

Cool hey?

------------------------------
_Oliv fecit, A.D. 2018._
