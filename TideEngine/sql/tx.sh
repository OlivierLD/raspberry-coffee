#!/bin/bash
while read one_line
do
	# starts with "select '" and does NOT end with "';"
    if [[ ${one_line} =~ ^(select \') && ! ${one_line} =~ (\';)$ ]]
    then
        echo "Found it in [${one_line}]"
        echo -e "${one_line}';"
    else
        # echo -e "${one_line}"
        echo -e "----"
    fi
done < $1
# echo -e "Done with $1"
 