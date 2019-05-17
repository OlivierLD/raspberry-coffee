FROM resin/raspberrypi3-debian:latest
#
# Minimal config
#
LABEL maintainer="Olivier LeDiouris <olivier@lediouris.net>"

RUN echo "alias ll='ls -lisah'" >> $HOME/.bashrc

RUN echo "git --version" >> $HOME/.bashrc
RUN echo "java -version" >> $HOME/.bashrc

RUN mkdir /workdir
WORKDIR /workdir

RUN java -version
RUN git --version

# TODO Wiring Pi, Pi4J ?...
# TODO i2cdetect, gpio, etc
