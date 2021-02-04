FROM openjdk:14-alpine
# For Raspberry Pi, comment the above, un-comment the 4 next lines
# FROM resin/raspberrypi3-debian:latest
# RUN apt-get update
# RUN apt-get install -y procps net-tools wget
# RUN wget https://github.com/bell-sw/Liberica/releases/download/11.0.2/bellsoft-jdk11.0.2-linux-arm32-vfp-hflt.deb
# RUN apt-get install ./bellsoft-jdk11.0.2-linux-arm32-vfp-hflt.deb
## RUN apt-get install -y default-jdk
# RUN java -version
#
COPY build/libs/mn-*-all.jar mn.jar
EXPOSE 8080
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "mn.jar"]