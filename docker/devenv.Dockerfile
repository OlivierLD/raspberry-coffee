#ARG http_proxy=""
#ARG https_proxy=""
#ARG no_proxy=""
#
ARG http_proxy="http://www-proxy.us.oracle.com:80"
ARG https_proxy="http://www-proxy.us.oracle.com:80"
ARG no_proxy=""
#
FROM debian
#
# To run on a laptop.
# Basic development environment
#
LABEL maintainer="Olivier LeDiouris <olivier@lediouris.net>"

# Uncomment if running behind a firewall (also set the proxies at the Docker level to the values below)
ENV http_proxy ${http_proxy}
ENV https_proxy ${https_proxy}
# ENV ftp_proxy $http_proxy
ENV no_proxy ${no_proxy}
#

# From the host to the image
# COPY bashrc $HOME/.bashrc

RUN echo "alias ll='ls -lisah'" >> $HOME/.bashrc

RUN apt-get update
RUN apt-get install -y curl git build-essential default-jdk
RUN curl -sL https://deb.nodesource.com/setup_12.x | bash -
RUN apt-get install -y nodejs
RUN apt-get install -y yarn
RUN apt-get install -y maven

RUN apt-get install -y procps net-tools wget

#RUN DEBIAN_FRONTEND=noninteractive apt-get install --fix-missing -y mate-desktop-environment-core tightvncserver vim
RUN DEBIAN_FRONTEND=noninteractive apt-get install --fix-missing -y tightvncserver vim
RUN mkdir ~/.vnc
RUN echo "mate" | vncpasswd -f >> ~/.vnc/passwd
RUN chmod 600 ~/.vnc/passwd

# RUN apt-get install -y chromium
# RUN apt-get install -y inkscape

RUN npm install -g grunt-cli
RUN npm install -g grunt-hub

# RUN apt-get install -y libgtk2.0-dev

EXPOSE 5901

RUN useradd -d /home/oliv -ms /bin/bash -g root -G sudo -p oliv oliv

RUN echo "git --version" >> $HOME/.bashrc
RUN echo "echo -n 'node:' && node -v" >> $HOME/.bashrc
RUN echo "echo -n 'npm:' && npm -v" >> $HOME/.bashrc
RUN echo "echo -n 'yarn:' && yarn --version" >> $HOME/.bashrc
RUN echo "echo -n 'maven:' && mvn -version" >> $HOME/.bashrc
RUN echo "java -version" >> $HOME/.bashrc
RUN echo "vncserver -version" >> $HOME/.bashrc
RUN echo "alias ll='ls -lisah'" >> $HOME/.bashrc

RUN echo "echo 'To start VNCserver, type: vncserver :1 -geometry 1280x800 -depth 24'" >> $HOME/.bashrc
RUN echo "echo '                       or vncserver :1 -geometry 1440x900 -depth 24'" >> $HOME/.bashrc
RUN echo "echo '                       or vncserver :1 -geometry 1680x1050 -depth 24, etc...'" >> $HOME/.bashrc
RUN echo "echo ' - Note: VNC password is mate'" >> $HOME/.bashrc

USER root
WORKDIR /home/root
RUN mkdir workdir
WORKDIR workdir

# From local file system to image
COPY ./dev ./dev
COPY ./gtk ./gtk
#    |     |
#    |     In the Docker image
#    On the host (this machine)
#
# RUN git clone https://github.com/OlivierLD/raspberry-coffee.git
# RUN git clone https://github.com/OlivierLD/WebComponents.git

#WORKDIR gtk
#RUN gcc `pkg-config --cflags --libs gtk+-2.0` gtktest.c -o gtktest

# WORKDIR workdir/dev

#ENV http_proxy ""
#ENV https_proxy ""
#ENV no_proxy ""

# EXPOSE 8080
# ENTRYPOINT ["npm", "start"]
# CMD ["vncserver", "-fg"]
