#!/usr/bin/env bash
#
echo -e "----------------------------------------------------------------------------------"
echo -e "Interactive tests, enter Z and Elev. values from the keyboard."
echo -e "- Use this to setup the right parameters for your hardware config:"
echo -e "  azimuth.ratio, elevation.ratio, azimuth.inverted, elevation.inverted, and others"
echo -e "----------------------------------------------------------------------------------"
#
CP=./build/libs/SunFlower-v2-1.0-all.jar
JAVA_OPTS=
# JAVA_OPTS="${JAVA_OPTS} -Ddevice.lat=37.7489 -Ddevice.lng=-122.5070"  # SF
JAVA_OPTS="${JAVA_OPTS} -Ddevice.lat=47.677677 -Ddevice.lng=-3.135667"  # Belz
JAVA_OPTS="${JAVA_OPTS} -Dazimuth.ratio=16:76"  # For V5
# JAVA_OPTS="${JAVA_OPTS} -Dazimuth.ratio=20:40"  # For V3
JAVA_OPTS="${JAVA_OPTS} -Delevation.ratio=18:128"
JAVA_OPTS="${JAVA_OPTS} -Dastro.verbose=false"
JAVA_OPTS="${JAVA_OPTS} -Dmotor.hat.verbose=false"
JAVA_OPTS="${JAVA_OPTS} -Dmoves.verbose=true"
JAVA_OPTS="${JAVA_OPTS} -Dtoo.long.exception.verbose=false"
#
JAVA_OPTS="${JAVA_OPTS} -Dazimuth.inverted=false"  # For V5
# JAVA_OPTS="${JAVA_OPTS} -Dazimuth.inverted=true"
#
JAVA_OPTS="${JAVA_OPTS} -Dcalibration=true"
#
JAVA_OPTS="${JAVA_OPTS} -Dsun.flower.verbose=true"
#
# Default is 30, can be increased if not MICROSTEP
JAVA_OPTS="${JAVA_OPTS} -Drpm=30"
#
JAVA_OPTS="${JAVA_OPTS} -Dstepper.style=MICROSTEP"
# JAVA_OPTS="${JAVA_OPTS} -Dstepper.style=SINGLE"
# JAVA_OPTS="${JAVA_OPTS} -Dstepper.style=DOUBLE"
# JAVA_OPTS="${JAVA_OPTS} -Dstepper.style=INTERLEAVE"
#
JAVA_OPTS="${JAVA_OPTS} -Duse.step.accumulation=true"
#
JAVA_OPTS="${JAVA_OPTS} -Dwith.ssd1306=true"
#
COMMAND="java -cp ${CP} ${JAVA_OPTS} sunflower.SunFlowerDriver"
echo "Running ${COMMAND}"
${COMMAND}
