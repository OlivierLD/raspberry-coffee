ARG http_proxy=""
ARG https_proxy=""
ARG no_proxy=""
#
#ARG http_proxy="http://www-proxy.us.oracle.com:80"
#ARG https_proxy="http://www-proxy.us.oracle.com:80"
#ARG no_proxy=""
#
FROM debian AS builder
#
# To run on a laptop - not necessaritly on an RPi (hence the default-jdk below)
# Demos the NavServer (Tide, Almanac, Weather faxes, etc)
# Clones the repo and recompiles everything, and archive it for production (step 2)
# proxy settings are passed as ARGs
#
LABEL maintainer="Olivier LeDiouris <olivier@lediouris.net>"
#
# Uncomment if running behind a firewall (also set the proxies at the Docker level to the values below)
ENV http_proxy ${http_proxy}
ENV https_proxy ${https_proxy}
# ENV ftp_proxy $http_proxy
ENV no_proxy ${no_proxy}
#

RUN echo "alias ll='ls -lisah'" >> $HOME/.bashrc

RUN \
  apt-get update && \
  apt-get upgrade -y && \
  DEBIAN_FRONTEND=noninteractive apt-get install --fix-missing -y curl git build-essential default-jdk zip && \
  rm -rf /var/lib/apt/lists/*

#RUN curl -sL https://deb.nodesource.com/setup_12.x | bash -
#RUN apt-get install -y nodejs

RUN mkdir /workdir
WORKDIR /workdir
RUN git clone https://github.com/OlivierLD/raspberry-coffee.git
WORKDIR /workdir/raspberry-coffee
RUN ./gradlew tasks
# RUN ./gradlew tasks -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80
WORKDIR /workdir/raspberry-coffee/NMEA.mux.WebUI/full.server
RUN ./builder.sh
RUN find . -name '*.gz'
# The step above has generated an NMEADist.tar.gz in NMEA.mux.WebUI/full.server
RUN echo "Build is done!"

# 2nd stage, build the runtime image
# FROM openjdk:8-jre-slim # This one not for Raspberry Pi
FROM resin/raspberrypi3-debian:latest

# Uncomment if running behind a firewall (also set the proxies at the Docker level to the values below)
ENV http_proxy ${http_proxy}
ENV https_proxy ${https_proxy}
# ENV ftp_proxy $http_proxy
ENV no_proxy ${no_proxy}

RUN echo "alias ll='ls -lisah'" >> $HOME/.bashrc
RUN echo "banner Nav Server" >> $HOME/.bashrc
RUN echo "java -version" >> $HOME/.bashrc

# RUN apt-get install -y default-jdk
RUN apt-get update
# RUN apt-get install -y oracle-java8-jdk
# RUN java -version
RUN apt-get install --fix-missing -y sysvbanner vim
# TODO Install librxtx-java ?

WORKDIR /navserver
COPY --from=builder /workdir/raspberry-coffee/NMEA.mux.WebUI/full.server/NMEADist.tar.gz ./

RUN tar -xzf NMEADist.tar.gz

#ENV http_proxy ""
#ENV https_proxy ""
#ENV no_proxy ""
WORKDIR /navserver/NMEADist
EXPOSE 9999
CMD [ "./start-mux.sh", "--no-background" ]

# To push to docker
# docker tag de1ddc31533e olivierlediouris/navserver:latest
# docker login --username olivierlediouris --password xxxxxx
# docker push olivierlediouris/navserver:latest
# The local image can be removed, and it can now be run like
# docker run olivierlediouris/navserver

