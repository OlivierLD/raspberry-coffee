#!/usr/bin/env bash
#
# This requires some libraries to be installed:
# sudo apt-get update
# then
# sudo apt-get install libgtk2.0-dev
# or
# sudo apt-get install libgtk2.0
#
# gcc `pkg-config --cflags --libs gtk+-2.0` gtktest.c -o gtktest
gcc $(pkg-config --cflags --libs gtk+-2.0) gtktest.c -o gtktest
