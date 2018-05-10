FROM debian
#
# To run on a laptop.
# Demos the WebComponents
#
LABEL maintainer="Olivier LeDiouris <olivier@lediouris.net>"

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

EXPOSE 5901

RUN echo "git --version" >> $HOME/.bashrc
RUN echo "echo -n 'node:' && node -v" >> $HOME/.bashrc
RUN echo "echo -n 'npm:' && npm -v" >> $HOME/.bashrc
RUN echo "java -version" >> $HOME/.bashrc
RUN echo "vncserver -version" >> $HOME/.bashrc

RUN mkdir /workdir
WORKDIR /workdir
RUN git clone https://github.com/OlivierLD/raspberry-pi4j-samples.git
WORKDIR /workdir/raspberry-pi4j-samples/WebComponents

EXPOSE 8080
# CMD ["npm", "start"]
# CMD ["vncserver", "-fg"]
