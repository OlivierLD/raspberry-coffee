#!/usr/bin/env bash
for log in 2018*
do
  cat $log >> kayak.estero.nmea
done
