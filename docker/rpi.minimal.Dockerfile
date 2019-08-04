FROM resin/raspberrypi3-debian:latest
#
# Minimal config
#
LABEL maintainer="Olivier LeDiouris <olivier@lediouris.net>"

RUN apt-get update
# RUN apt-get install -y sysvbanner
# RUN apt-get install -y curl git build-essential
RUN apt-get install -y oracle-java8-jdk
# RUN curl -sL https://deb.nodesource.com/setup_9.x | bash -
# RUN apt-get install -y nodejs
# RUN apt-get install -y librxtx-java

RUN echo "alias ll='ls -lisah'" >> $HOME/.bashrc

RUN echo "git --version" >> $HOME/.bashrc
RUN echo "java -version" >> $HOME/.bashrc

RUN mkdir /workdir
WORKDIR /workdir

RUN java -version
RUN git --version

# TODO Wiring Pi, Pi4J ?...
# TODO i2cdetect, gpio, etc
