#!/usr/bin/env bash
gcc `pkg-config --cflags --libs gtk+-2.0` gtktest.c -o gtktest
