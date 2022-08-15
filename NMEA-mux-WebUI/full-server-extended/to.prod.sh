#!/usr/bin/env bash
#
# Warning: Run the process on the target machine. That will avoid unwanted version mismatch (java class version...)
#
echo -e "+----------------------------------------------------------------------------------------------------+"
echo -e "|                          P A C K A G E   f o r   D I S T R I B U T I O N                           |"
echo -e "+----------------------------------------------------------------------------------------------------+"
echo -e "| This is an example showing how to generate a 'production' version, without the full github repo,   |"
echo -e "| just what is needed to run the NMEA Multiplexer - in several configurations - and its web clients. |"
echo -e "+----------------------------------------------------------------------------------------------------+"
echo -e "| Now starting a fresh build...                                                                      |"
echo -e "| Make sure the java version is compatible with your target                                          |"
java -version
echo -e "+----------------------------------------------------------------------------------------------------+"
#
# 1 - Build
#
PROXY_SETTINGS=
PROXY_SETTINGS="-Dhttp.proxyHost=www-proxy-hqdc.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy-hqdc.us.oracle.com -Dhttps.proxyPort=80"
../../gradlew shadowJar ${PROXY_SETTINGS}
#
# 2 - Create new dir
#
echo -en "Which (non existent) folder should we create the distribution in ? > "
# Directory name, that will become the archive name.
read distdir
if [[ -d "${distdir}" ]]; then
	echo -e "Folder ${distdir} exists. Please drop it or choose another name"
	exit 1
fi
echo -e "Creating folder ${distdir}"
mkdir ${distdir}
mkdir ${distdir}/build
mkdir ${distdir}/build/libs
#
# 3 - Copying needed resources
#
echo -e "Copying resources"
cp ./build/libs/*-1.0-all.jar ${distdir}/build/libs
# Log folder
mkdir ${distdir}/logged
# Web resources
cp web.zip ${distdir}
# Properties files
cp *.properties ${distdir}
cp *.yaml ${distdir}
# If needed, more resources would go here (like dev curves, etc)
cp mux.sh ${distdir}
cp to.mux.sh ${distdir}
cp ssd1306i2cDisplay.sh ${distdir}
cp killmux.sh ${distdir}
cp rc.local ${distdir}
cp start-mux.sh ${distdir}
#
# 4 - Archiving
#
# zip -q -r ${distdir}.zip ${distdir}
tar -cvzf ${distdir}.tar.gz ${distdir}
rm -rf ${distdir}
#
# 5 - Ready!
#
echo -e "+--------------------------------------------------------------------------------------------------+"
echo -e " >> Archive ${distdir}.tar.gz ready for deployment."
echo -e "+--------------------------------------------------------------------------------------------------+"
echo -e "| Send it to another machine, and un-archive it.                                                   |"
echo -e "| To transfer, use  command like   \$ scp ${distdir}.tar.gz pi@192.168.42.8:~/                       "
echo -e "| Use 'tar -xz[v]f ${distdir}.tar.gz' to un-archive.                                                 "
echo -e "| External dependencies like librxtx-java may be needed if you intend to use a serial port,        |"
echo -e "| in which case you may need to run a 'sudo apt-get install librxtx-java' .                        |"
echo -e "| The script to launch will be 'mux.sh'                                                            |"
echo -e "| It is your responsibility to use the right properties file, possibly modified to fit your needs. |"
echo -e "| For the runner/logger, use nmea.mux.gps.log.properties                                           |"
echo -e "| Use it for example like:                                                                         |"
echo -e "| $ nohup ./mux.sh nmea.mux.gps.log.properties &                                                   |"
echo -e "+--------------------------------------------------------------------------------------------------+"
