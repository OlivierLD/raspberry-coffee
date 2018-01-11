#!/bin/bash
echo To install the transpiler, run:
echo sudo npm install -g es6-module-transpiler
#
echo Now transpiling
compile-modules convert -I . -o out modules.01.js modules.consume.js --format commonjs
#
echo Now running
cd out
node modules.consume.js
