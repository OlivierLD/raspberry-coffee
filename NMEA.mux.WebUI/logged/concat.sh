#!/usr/bin/env bash
#
# Use this script to concatenate all the nmea files in the current directory
# into a single file. The name of the single file is to be provided as 1st parameter of the script.
#
full_file=$1
if [ "$full_file" == "" ]
then
  echo -e "Error: Provide the name of the file to produce as first parameter."
  echo -e "Usage is: "
  echo -e "$ $0 big.nmea"
  exit 1
fi
if [ -f "$full_file" ]; then
  echo -e "Warning: $full_file already exist"
  echo "Choose another file name"
  exit 1
fi
for chunk in `ls *.nmea`
do
#  echo $chunk
  cat $chunk >> $full_file
done
echo -e "$full_file ready"
