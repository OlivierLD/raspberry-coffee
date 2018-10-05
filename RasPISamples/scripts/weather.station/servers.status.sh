#!/usr/bin/env bash
#
echo "-- Checking if required processes are alive --"
echo "-- NMEA Mux --"
ps -ef | grep nmea.mux.GenericNMEAMultiplexer | grep -v grep
echo "-- REST Nav Server --"
ps -ef | grep navrest.NavServer | grep -v grep
echo "-- Email Watcher --"
ps -ef | grep EmailWatcher | grep -v grep
echo "-- Snap Loop --"
ps -ef | grep snap.loop.sh | grep -v grep
echo "---------------"
#
