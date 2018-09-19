## Project Trunk
This folder contains projects involving the components available in other modules of this project.
> Note: Will eventually replace the content of `RasPISamples`.

There are several of them - as opposed to just a big one - to minimize the number
of dependencies for each example.

They all come with a `build.gradle`, and the examples are built by a
```
 $ ../../gradlew shadowJar
```
and a script to run the example should be available as well.

- [Buttons, Reflex Game](Button.Reflex)
- [PurePWM](PurePWM)
- [System resolution](System.Languages)
    - [System resolution, in several languages](System.Languages/LanguageComparison.md)
- [Weather Station](Weather.Station)
- [Radar](RasPiRadar)

---
