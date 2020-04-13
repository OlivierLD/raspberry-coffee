#!/bin/bash
#
# See https://www.cs.fsu.edu/~myers/howto/g++compiling.txt
#
gcc --version
# g++ -c MathUtils.cpp Earth.cpp Venus.cpp Mars.cpp Jupiter.cpp Saturn.cpp AstroComputer.cpp
g++ *.cpp -o astro
