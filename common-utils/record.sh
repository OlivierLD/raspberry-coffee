#!/bin/bash
# Extract temperature and voltage from sysinfo.sh
# Use it like ./record.sh >> recording.txt
LOOP=true
while ("${LOOP}" == "true")
do
    ./sysinfo.sh | grep -E 'temp=|volt=' > snap.txt
    #
    while read line
    do
        # echo $line
        if [[ "${line}" == *"temp"* ]]
        then
            temp=${line#*=}
            temp=${temp::-2}
            # echo -e "Temp: ${temp}"
        elif [[ "${line}" == *"volt"* ]]
        then
            volt=${line#*=}
            volt=${volt::-1}
            # echo -e "Volt: ${volt}"
        fi
    done < snap.txt
    #
    echo -e "{ \"temp\": ${temp}, \"volt\": ${volt} }"
    #
    sleep 1
done
