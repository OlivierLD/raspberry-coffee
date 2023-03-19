#!/bin/bash
echo -e "pkg-config --cflags --libs gtk+-2.0 :"
pkg-config --cflags --libs gtk+-2.0
#
# Works on Mac
# gcc `pkg-config --cflags --libs gtk+-2.0` gtktest.c -o gtktest
# gcc $(pkg-config --cflags --libs gtk+-2.0) gtktest.c -o gtktest
#
# Works on Raspberry Pi and Mac
gcc -o gtktest gtktest.c $(pkg-config gtk+-2.0 --cflags --libs)
#
