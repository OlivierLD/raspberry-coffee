FROM debian

LABEL maintainer="Olivier LeDiouris <olivier@lediouris.net>"

# ENV http_proxy http://www-proxy.us.oracle.com:80
# ENV https_proxy http://www-proxy.us.oracle.com:80

# From the host to the image
ADD nodepi.banner.sh /

# RUN chmod +x /nodepi.banner.sh
# RUN echo "/nodepi.banner.sh" >> $HOME/.bashrc

RUN apt-get update
RUN apt-get install -y sysvbanner
RUN apt-get install -y curl git build-essential
RUN curl -sL https://deb.nodesource.com/setup_12.x | bash -
RUN apt-get install -y nodejs

# RUN useradd -ms /bin/bash oliv
# RUN mkdir /home/oliv

# USER oliv
RUN echo "alias ll='ls -lisah'" >> $HOME/.bashrc
RUN echo "banner Node-PI" >> $HOME/.bashrc

RUN echo "git --version" >> $HOME/.bashrc
RUN echo "echo -n 'node:' && node -v" >> $HOME/.bashrc
RUN echo "echo -n 'npm:' && npm -v" >> $HOME/.bashrc

RUN mkdir /workdir
WORKDIR /workdir
RUN git clone https://github.com/OlivierLD/node.pi.git
WORKDIR /workdir/node.pi
RUN npm install
# RUN npm install -g node-inspector

EXPOSE 9876
CMD ["npm", "start"]
