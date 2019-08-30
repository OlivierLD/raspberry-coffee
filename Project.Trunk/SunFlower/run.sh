#!/bin/bash
#
# SunFlower launching script
#
CP=./build/libs/SunFlower-1.0-all.jar
#
echo Try $0 -help or $0 --help
#
echo -en "Date is "
date
echo -en "Is that OK [Y]|n ? > "
read resp
if [ "$resp" == "n" ] || [ "$resp" == "N" ]
then
  echo -en "Enter new date, format '10 NOV 2018 08:55:00' > "
  read newDate
  if [ "$newDate" != "" ]
  then
    sudo date -s "$newDate"
   fi
fi
#
# You can use several servos for heading, several servos for tilt,
# in case you need to orient several solar panels. Use a comma-separated list of channels.
#
HEADING_SERVO_ID=14 #,0,10
TILT_SERVO_ID=15    #,1
#
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Dlatitude=37.7489 -Dlongitude=-122.5070" # SF.
# JAVA_OPTS="$JAVA_OPTS -Dlatitude=22.0616180555556 -Dlongitude=-159.378951111111" # Kauai.
JAVA_OPTS="$JAVA_OPTS -DdeltaT=68.9677" # 01-Jan-2018
#
JAVA_OPTS="$JAVA_OPTS -Dtest.servos=false"
#
JAVA_OPTS="$JAVA_OPTS -Dtilt.servo.sign=1"
JAVA_OPTS="$JAVA_OPTS -Dheading.servo.sign=1"
#
JAVA_OPTS="$JAVA_OPTS -Dtilt.limit=40"
JAVA_OPTS="$JAVA_OPTS -Dtilt.offset=0"
#
OPTION=-help
WITH_HELP=false
if [ $# -gt 0 ]
then
  OPTION=$1
fi
if [ $OPTION = "-help" -o $OPTION = "--help" ] && [ $# -gt 1 ]
then
  OPTION=$2
  WITH_HELP=true
fi
#
displayHelp() {
  echo -e "=============================================================================="
  echo -e "Available CLI options are -help, basic, plus, verbose, demo, smooth, resthttp."
  echo -e " Use $0 -help [option] to know more about options, like:"
  echo -e " $ $0 -help resthttp "
  echo -e "=============================================================================="
}
#
if [ $WITH_HELP = true ]
then
  echo -e "+-----------------------------"
  echo -e "| Explaining option $OPTION"
  echo -e "+-----------------------------"
else
  echo Using option "$OPTION"
fi
#
case "$OPTION" in
  "-help" | "?" | "-h" | "help" | "--help")
    displayHelp
    exit 0
    ;;
  "basic")
    JAVA_OPTS="$JAVA_OPTS -Dadc.verbose=false"
    JAVA_OPTS="$JAVA_OPTS -Dorient.verbose=false"
    JAVA_OPTS="$JAVA_OPTS -Dastro.verbose=false"
    JAVA_OPTS="$JAVA_OPTS -Dtilt.verbose=false"
    JAVA_OPTS="$JAVA_OPTS -Dservo.super.verbose=none"
    JAVA_OPTS="$JAVA_OPTS -Dmanual.entry=false"
    JAVA_OPTS="$JAVA_OPTS -Dansi.console=false"
    #
    JAVA_OPTS="$JAVA_OPTS -Dsmooth.moves=false"
    JAVA_OPTS="$JAVA_OPTS -Ddemo.mode=false"
    ;;
  "plus")
    JAVA_OPTS="$JAVA_OPTS -Dadc.verbose=true"
    JAVA_OPTS="$JAVA_OPTS -Dorient.verbose=true"
    JAVA_OPTS="$JAVA_OPTS -Dastro.verbose=true"
    JAVA_OPTS="$JAVA_OPTS -Dtilt.verbose=false"
    JAVA_OPTS="$JAVA_OPTS -Dservo.super.verbose=heading"
    JAVA_OPTS="$JAVA_OPTS -Dmanual.entry=false"
    JAVA_OPTS="$JAVA_OPTS -Dansi.console=false"
    #
    JAVA_OPTS="$JAVA_OPTS -Dsmooth.moves=true"
    JAVA_OPTS="$JAVA_OPTS -Ddemo.mode=false"
    JAVA_OPTS="$JAVA_OPTS -Done.by.one=false"
    ;;
  "verbose")
    JAVA_OPTS="$JAVA_OPTS -Dadc.verbose=true"
    JAVA_OPTS="$JAVA_OPTS -Dorient.verbose=true"
    JAVA_OPTS="$JAVA_OPTS -Dastro.verbose=true"
    JAVA_OPTS="$JAVA_OPTS -Dtilt.verbose=true"
    JAVA_OPTS="$JAVA_OPTS -Dservo.super.verbose=true"
    JAVA_OPTS="$JAVA_OPTS -Dmanual.entry=false"
    JAVA_OPTS="$JAVA_OPTS -Dansi.console=false"
    #
    JAVA_OPTS="$JAVA_OPTS -Dsmooth.moves=false"
    JAVA_OPTS="$JAVA_OPTS -Ddemo.mode=false"
    ;;
  "demo")
    JAVA_OPTS="$JAVA_OPTS -Dadc.verbose=true"
    JAVA_OPTS="$JAVA_OPTS -Dorient.verbose=true"
    JAVA_OPTS="$JAVA_OPTS -Dastro.verbose=true"
    JAVA_OPTS="$JAVA_OPTS -Dtilt.verbose=false"
    JAVA_OPTS="$JAVA_OPTS -Dservo.super.verbose=false"
    JAVA_OPTS="$JAVA_OPTS -Dmanual.entry=false"
    JAVA_OPTS="$JAVA_OPTS -Dansi.console=true"
    #
    JAVA_OPTS="$JAVA_OPTS -Dlog.battery.data=false"   # Console output
    JAVA_OPTS="$JAVA_OPTS -Dlog.photocell.data=false" # Console output
    JAVA_OPTS="$JAVA_OPTS -Dsmooth.moves=false"
    JAVA_OPTS="$JAVA_OPTS -Ddemo.mode=true"
    # The 2 following ones are required if demo.mode=true
    JAVA_OPTS="$JAVA_OPTS -Dfrom.date=2017-06-28T05:53:00"
    JAVA_OPTS="$JAVA_OPTS -Dto.date=2017-06-28T20:33:00"
    ;;
  "smooth")
    JAVA_OPTS="$JAVA_OPTS -Dadc.verbose=false"
    JAVA_OPTS="$JAVA_OPTS -Dorient.verbose=true"
    JAVA_OPTS="$JAVA_OPTS -Dastro.verbose=true"
    JAVA_OPTS="$JAVA_OPTS -Dtilt.verbose=true"
    JAVA_OPTS="$JAVA_OPTS -Dservo.super.verbose=false"
    JAVA_OPTS="$JAVA_OPTS -Dmanual.entry=false"
    JAVA_OPTS="$JAVA_OPTS -Dansi.console=true"
    #
    JAVA_OPTS="$JAVA_OPTS -Dsmooth.moves=true"
    JAVA_OPTS="$JAVA_OPTS -Done.by.one=false"
    JAVA_OPTS="$JAVA_OPTS -Ddemo.mode=false"
    ;;
  "resthttp") # With smooth moves, non-interactive
    JAVA_OPTS="$JAVA_OPTS -Dadc.verbose=false"
    JAVA_OPTS="$JAVA_OPTS -Ddisplay.digit=false"
    JAVA_OPTS="$JAVA_OPTS -Dorient.verbose=false"
    JAVA_OPTS="$JAVA_OPTS -Dastro.verbose=false"
    JAVA_OPTS="$JAVA_OPTS -Dtilt.verbose=false"
    JAVA_OPTS="$JAVA_OPTS -Dservo.super.verbose=false" # tilt, heading, none, false, both, true.
    JAVA_OPTS="$JAVA_OPTS -Dmanual.entry=false"
    JAVA_OPTS="$JAVA_OPTS -Dansi.console=false"
    #
    JAVA_OPTS="$JAVA_OPTS -Dsmooth.moves=true"
    JAVA_OPTS="$JAVA_OPTS -Done.by.one=false"
    JAVA_OPTS="$JAVA_OPTS -Ddemo.mode=false"
    #
    JAVA_OPTS="$JAVA_OPTS -Dhttp.port=9999"
    JAVA_OPTS="$JAVA_OPTS -Dinteractive=false"
    #
    JAVA_OPTS="$JAVA_OPTS -Dlog.battery.data=false"   # Console output
    JAVA_OPTS="$JAVA_OPTS -Dlog.photocell.data=false" # Console output
    ;;
  *)
    echo "======================="
    echo "Unknown option $OPTION."
    displayHelp
    exit 1
    ;;
esac
#
if [ $WITH_HELP = false ]
then
	MISO=9
	MOSI=10
	CLK=11
	CS=8
	BAT_CHANNEL=0
	PHOTO_CHANNEL=1
	#
	ADC_PRM="--with-adc:true --with-photocell:true --miso:$MISO --mosi:$MOSI --clk:$CLK --cs:$CS --battery-channel:$BAT_CHANNEL --photo-cell-channel:$PHOTO_CHANNEL"
	# sudo is required when running on the RPi.
  # nohup sudo java -cp $CP $JAVA_OPTS orientation.SunFlower --heading:$HEADING_SERVO_ID --tilt:$TILT_SERVO_ID --with-adc:false --with-photocell:false &
  COMMAND="java -cp $CP $JAVA_OPTS orientation.SunFlower --heading:$HEADING_SERVO_ID --tilt:$TILT_SERVO_ID $ADC_PRM"
  echo -e "Executing $COMMAND"
  sudo $COMMAND
else
  opts=$(echo $JAVA_OPTS | tr ";" "\n")
  for opt in $opts
  do
    echo -e "| $opt"
  done
  echo -e "+-----------------------------"
fi

