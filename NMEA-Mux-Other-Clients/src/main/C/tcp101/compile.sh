#!/bin/bash
#
echo -e "Compiling $(ls *.c)"
#
echo -e "-- GCC Version --"
gcc --version
echo -e "-----------------"
#
DEBUG_FLAG=""
echo -en "Enable _DEBUG flag y|n ? > "
read RESP
if [[  ${RESP} =~ ^(yes|y|Y)$ ]]; then
   DEBUG_FLAG="-D_DEBUG"
fi
# Here we go
gcc ${DEBUG_FLAG} *.c -o tcpClient
#
if [[ $? -ne 0 ]]; then
    echo "Compilation failed!.."
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
# ./run.sh
echo -e "Type [Ctrl+C] to stop the program"
./tcpClient
