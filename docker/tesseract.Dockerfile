FROM ubuntu:18.04
# See
# https://github.com/tesseract-shadow/tesseract-ocr-re
#
# Uncomment the ENV lines if a proxy is needed.
#
# ENV http_proxy http://www-proxy.us.oracle.com:80
# ENV https_proxy http://www-proxy.us.oracle.com:80
#
RUN echo "alias ll='ls -lisah'" >> $HOME/.bashrc
RUN apt-get update
RUN apt-get install -y sysvbanner
RUN echo "banner Tesseract" >> $HOME/.bashrc
#
RUN apt-get update && apt-get install -y software-properties-common && add-apt-repository -y ppa:alex-p/tesseract-ocr
RUN apt-get update && apt-get install -y tesseract-ocr-all

RUN mkdir /home/work
WORKDIR /home/work
