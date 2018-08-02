FROM debian
#
# To run on a laptop - not necessaritly on an RPi (hence the default-jdk below)
# Demos the NavServer (Tide, Almanac, Weather faxes, etc)
# Clones the repo and recompiles everything.
# Comment/Uncomment the ENV lines if needed (proxy)
#
LABEL maintainer="Olivier LeDiouris <olivier@lediouris.net>"

#
# Uncomment if running behind a firewall (also set the proxies at the Docker level to the values below)
ENV http_proxy http://www-proxy.us.oracle.com:80
ENV https_proxy http://www-proxy.us.oracle.com:80
# ENV ftp_proxy $http_proxy
ENV no_proxy "localhost,127.0.0.1,orahub.oraclecorp.com,artifactory-slc.oraclecorp.com"
#

RUN echo "alias ll='ls -lisah'" >> $HOME/.bashrc

RUN \
  apt-get update && \
  apt-get upgrade -y && \
  DEBIAN_FRONTEND=noninteractive apt-get install --fix-missing -y curl git build-essential default-jdk sysvbanner vim && \
  rm -rf /var/lib/apt/lists/*

RUN curl -sL https://deb.nodesource.com/setup_9.x | bash -
RUN apt-get install -y nodejs

RUN echo "banner Nav Server" >> $HOME/.bashrc
RUN echo "git --version" >> $HOME/.bashrc
RUN echo "echo -n 'node:' && node -v" >> $HOME/.bashrc
RUN echo "echo -n 'npm:' && npm -v" >> $HOME/.bashrc
RUN echo "java -version" >> $HOME/.bashrc

RUN mkdir /workdir
WORKDIR /workdir
RUN git clone https://github.com/OlivierLD/raspberry-pi4j-samples.git
WORKDIR /workdir/raspberry-pi4j-samples
RUN ./gradlew tasks
# RUN ./gradlew tasks -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80
WORKDIR /workdir/raspberry-pi4j-samples/RESTNavServer
RUN ../gradlew shadowJar
# RUN ../gradlew shadowJar -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80

ENV http_proxy ""
ENV https_proxy ""
ENV no_proxy ""

EXPOSE 9999
CMD ["./runNavServer"]
