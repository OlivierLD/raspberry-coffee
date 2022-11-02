#!/bin/bash
pkg-config --cflags --libs gtk+-2.0
#
# gcc `pkg-config --cflags --libs gtk+-2.0` gtktest.c -o gtktest
gcc $(pkg-config --cflags --libs gtk+-2.0) gtktest.c -o gtktest
#
