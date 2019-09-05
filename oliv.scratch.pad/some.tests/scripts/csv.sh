#!/usr/bin/env bash
DESTINATION_FOLDER=new_files
if [ -d $DESTINATION_FOLDER ]
then
	echo -en "Folder new_files exists, drop it Y|N ? > "
	read a
	if [ "$a" == "Y" ] || [ "$a" == "y" ]
	then
		rm -rf $DESTINATION_FOLDER
		mkdir $DESTINATION_FOLDER
	fi
else	
	mkdir $DESTINATION_FOLDER
fi
#
# Process the data
#
for datafile in ~/Desktop/*.csv
do
	echo -e "Processing $datafile into $DESTINATION_FOLDER/$(basename -- $datafile)"
	sed 's/,/;/g' $datafile > $DESTINATION_FOLDER/$(basename -- $datafile)
	# sed 's/,/;/g' $datafile
done
open $DESTINATION_FOLDER
echo -e "Done"
