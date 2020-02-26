ARG http_proxy=""
ARG https_proxy=""
ARG no_proxy=""
#
#ARG http_proxy="http://www-proxy.us.oracle.com:80"
#ARG https_proxy="http://www-proxy.us.oracle.com:80"
#ARG no_proxy=""
#
FROM debian
#
# To run on a laptop.
# Demos the WebComponents
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

RUN echo "git --version" >> $HOME/.bashrc
RUN echo "echo -n 'node:' && node -v" >> $HOME/.bashrc
RUN echo "echo -n 'npm:' && npm -v" >> $HOME/.bashrc
RUN echo "java -version" >> $HOME/.bashrc

RUN mkdir /workdir
WORKDIR /workdir
RUN git clone https://github.com/OlivierLD/WebComponents.git
WORKDIR /workdir/WebComponents/oliv-components/images
RUN ./changext.sh

WORKDIR /workdir/WebComponents
#ENV http_proxy ""
#ENV https_proxy ""
#ENV no_proxy ""

EXPOSE 8080
CMD ["npm", "start"]
