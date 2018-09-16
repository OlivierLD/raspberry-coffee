FROM debian
#
# To run on a laptop.
# Demoes the WebComponents.
# With VNC
#
LABEL maintainer="Olivier LeDiouris <olivier@lediouris.net>"

# Following lines may be commented.
ENV http_proxy http://www-proxy.us.oracle.com:80
ENV https_proxy http://www-proxy.us.oracle.com:80
## ENV ftp_proxy $http_proxy
ENV no_proxy "localhost,127.0.0.1,orahub.oraclecorp.com,artifactory-slc.oraclecorp.com"

# From the host to the image
# COPY bashrc $HOME/.bashrc

RUN echo "alias ll='ls -lisah'" >> $HOME/.bashrc

RUN apt-get update
RUN apt-get install -y curl git build-essential default-jdk
RUN curl -sL https://deb.nodesource.com/setup_9.x | bash -
RUN apt-get install -y nodejs
RUN apt-get install -y procps net-tools wget

RUN DEBIAN_FRONTEND=noninteractive apt-get install --fix-missing -y mate-desktop-environment-core tightvncserver
RUN mkdir ~/.vnc
RUN echo "mate" | vncpasswd -f >> ~/.vnc/passwd
RUN chmod 600 ~/.vnc/passwd

RUN apt-get install -y chromium

EXPOSE 5901

RUN useradd -d /home/oliv -ms /bin/bash -g root -G sudo -p oliv oliv

RUN echo "git --version" >> $HOME/.bashrc
RUN echo "echo -n 'node:' && node -v" >> $HOME/.bashrc
RUN echo "echo -n 'npm:' && npm -v" >> $HOME/.bashrc
RUN echo "java -version" >> $HOME/.bashrc
RUN echo "vncserver -version" >> $HOME/.bashrc
RUN echo "alias ll='ls -lisah'" >> $HOME/.bashrc

RUN echo "echo 'To start VNCserver, type: vncserver :1 -geometry 1280x800 (or 1440x900, 1680x1050, etc) -depth 24'" >> $HOME/.bashrc

USER root
WORKDIR /home/root
RUN mkdir workdir
WORKDIR workdir
RUN git clone https://github.com/OlivierLD/raspberry-pi4j-samples.git
WORKDIR raspberry-pi4j-samples/WebComponents

EXPOSE 8080
CMD ["npm", "start"]
# CMD ["vncserver", "-fg"]
