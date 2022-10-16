#!/bin/bash
#
echo -e "Compiling $(ls *.c)"
#
echo -e "-- GCC Version --"
gcc --version
echo -e "-----------------"
#
gcc *.c -o httpClient
#
if [ $? -ne 0 ]; then
    echo "Compile failed!.."
    exit 1
else
    echo "Compilation OK"
fi
#
echo -en "Do we run the program  y|n ? > "
read RESP
if [[ ! ${RESP} =~ ^(yes|y|Y)$ ]]; then
   echo -e "OK, bye!"
   exit 0
fi
#
./run.sh
