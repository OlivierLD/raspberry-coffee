FROM resin/raspberrypi3-debian:latest
#
# NMEA Multiplexer running on the Raspberry Pi.
# Reads a GPS from serial port, forward to a file and a small OLED screen
# Web and REST interfaces available.
#
# NodeJS is installed, but not used here.
#
LABEL maintainer="Olivier LeDiouris <olivier@lediouris.net>"

# ENV http_proxy http://www-proxy.us.oracle.com:80
# ENV https_proxy http://www-proxy.us.oracle.com:80

RUN echo "alias ll='ls -lisah'" >> $HOME/.bashrc

RUN apt-get update
RUN apt-get install -y sysvbanner
RUN apt-get install -y curl git build-essential
# RUN apt-get install -y default-jdk
RUN apt-get install -y oracle-java8-jdk
RUN curl -sL https://deb.nodesource.com/setup_9.x | bash -
RUN apt-get install -y nodejs
RUN apt-get install -y librxtx-java
# RUN apt-get install -y xvfb procps net-tools wget

RUN echo "banner GPS-PI Mux" >> $HOME/.bashrc
RUN echo "git --version" >> $HOME/.bashrc
RUN echo "echo -n 'node:' && node -v" >> $HOME/.bashrc
RUN echo "echo -n 'npm:' && npm -v" >> $HOME/.bashrc
RUN echo "java -version" >> $HOME/.bashrc

RUN mkdir /workdir
WORKDIR /workdir
RUN git clone https://github.com/OlivierLD/raspberry-coffee.git
WORKDIR /workdir/raspberry-coffee
# Running gradle with 'tasks' will install gradle and required plugins if not there yet.
RUN ./gradlew tasks
# RUN ./gradlew tasks -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80
#
WORKDIR /workdir/raspberry-coffee/NMEA.multiplexer
#
RUN ../gradlew shadowJar
# RUN ../gradlew shadowJar -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80

# ENV http_proxy ""
# ENV https_proxy ""

EXPOSE 9999
# We are located in /workdir/raspberry-coffee/NMEA.multiplexer
#
# With I2C SSD1306
# CMD ["./mux.sh", "nmea.mux.gps.log.properties"]
#
# Without I2C SSD1306
CMD ["./mux.sh", "nmea.mux.gps.log.small.properties"]
