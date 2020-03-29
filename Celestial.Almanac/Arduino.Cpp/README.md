# C and friends
## Arduino
This was originally an attempt to write the Celestial Computer for the Arduino.

As it is now, it is way too big to fit the memory available on an Arduino.

You can use the script `mklib.sh` to build a library for Arduino, but the files it contains are ** way too big** to be used.

It is OK for C++ though. See the `compile.sh` in the `AstroLib` folder (more below).

## C++
This being said, C++ works OK.

Look in to then `AstroLib` folder, run the script `compile.sh`, and you should be able to run the `./astro` generated executable.

Do look into `sample_main.cpp` to see how this is done, how to use the `AstroComputer` for yourself.

---
