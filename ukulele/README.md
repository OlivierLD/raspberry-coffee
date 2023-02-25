# Ukulele 101
Originally written as applets ... Mostly not supported anymore.  
Moving to a Java Swing interface to keep the existing code running.

Uses **[JFugue](http://www.jfugue.org/)** for the sound part.
<br>
We currently use the version 4.0.3 of JFugue. The last one is 5.0.5, a little bit different.
Currently working on updating it, keep posted.

## Built it
```
$ ../gradlew clean shadowJar
```

## Run it
Start with 
```
$ ./run.1.sh [FR]
```
and
```
$ ./run.2.sh [FR]
```

or run it from `gradle`:
```
$ ../gradle chordFinder
$ ../gradle keyChordFinder
```
---
