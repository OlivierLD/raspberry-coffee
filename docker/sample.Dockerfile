FROM debian

LABEL maintainer="Olivier LeDiouris <olivier.lediouris@oracle.com>"

ENV http_proxy http://www-proxy.us.oracle.com:80
ENV https_proxy http://www-proxy.us.oracle.com:80
ENV no_proxy oraclecorp.com

RUN apt-get update
RUN apt-get install -y sysvbanner
RUN apt-get install -y git build-essential
# RUN curl -sL https://deb.nodesource.com/setup_9.x | bash -
# RUN apt-get install -y nodejs

# RUN curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add -
# RUN echo "deb https://dl.yarnpkg.com/debian/ stable main" | tee /etc/apt/sources.list.d/yarn.list
# RUN apt-get install -y yarn

# USER oliv
RUN echo "alias ll='ls -lisah'" >> $HOME/.bashrc
RUN echo "banner 1PaaS-UI Examples" >> $HOME/.bashrc

RUN echo "git --version" >> $HOME/.bashrc
# RUN echo "echo -n 'node:' && node -v" >> $HOME/.bashrc
# RUN echo "echo -n 'npm:' && npm -v" >> $HOME/.bashrc
# RUN echo "echo -n 'yarn:' && yarn --version" >> $HOME/.bashrc

RUN mkdir /workdir
WORKDIR /workdir
RUN git clone https://orahub.oraclecorp.com/one-paas-dev/one-paas-ui-examples.git
#WORKDIR /workdir/one-paas-ui-examples/3.2
## RUN yarn
#RUN npm install
#WORKDIR /workdir/one-paas-ui-examples/4.2
## RUN yarn
#RUN npm install
#WORKDIR /workdir/one-paas-ui-examples/
## RUN npm install -g node-inspector

EXPOSE 8080
#CMD ["node", "server.js"]
