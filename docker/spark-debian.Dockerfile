FROM debian:latest

#
# Java 11, Scala 2.12.12, and Spark 3.0.1 (or more recent), on Debian
# Updated Oct 2020
#

LABEL maintainer="Olivier LeDiouris <olivier@lediouris.net>"

# Will set the proxy in the image.
# Might be needed to set it at the Docker level too (when building the image), to pull the base image for example.
#
# ENV http_proxy http://www-proxy.us.oracle.com:80
# ENV https_proxy http://www-proxy.us.oracle.com:80
# ENV ftp_proxy http://etc...
# ENV no_proxy "*.oracle.com,.home.net"

ENV SCALA_VERSION 2.13.8
# ENV SCALA_VERSION 2.12.12
# ENV SCALA_VERSION 2.12.6
ENV SCALA_TARBALL http://www.scala-lang.org/files/archive/scala-$SCALA_VERSION.deb

# See https://spark.apache.org/downloads.html
# -------------------------------------------
# ENV APACHE_MIRROR https://mirrors.gigenet.com/apache/spark/spark-3.0.1/
# ENV APACHE_MIRROR https://mirrors.ocf.berkeley.edu/apache/spark/spark-3.0.1/
ENV APACHE_MIRROR https://dlcdn.apache.org/spark/spark-3.1.2/
# ENV APACHE_MIRROR https://mirrors.gigenet.com/apache/spark/spark-3.0.0-preview2/
# ENV SPARK_TARBALL http://apache.claz.org/spark/spark-2.3.0/spark-2.3.0-bin-hadoop2.7.tgz
# ENV SPARK_TARBALL $APACHE_MIRROR/spark-3.0.0-preview2-bin-hadoop3.2.tgz
# ENV SPARK_TARBALL $APACHE_MIRROR/spark-3.0.1-bin-hadoop2.7-hive1.2.tgz
ENV SPARK_TARBALL $APACHE_MIRROR/spark-3.1.2-bin-hadoop3.2.tgz

RUN apt-get update
RUN apt-get install -y sysvbanner
RUN apt-get install -y curl git build-essential default-jdk libssl-dev libffi-dev python-dev vim
# RUN apt-get install -y python-pip python-dev
RUN apt-get install -y python3-pip python3-dev python3-venv
RUN pip3 install pandas numpy scipy scikit-learn
#
RUN apt-get install -y cmake unzip pkg-config libopenblas-dev liblapack-dev
# RUN apt-get install -y python-numpy python-scipy python-matplotlib python-yaml
RUN python3 -mpip install matplotlib
#
RUN pip3 install jupyter
RUN pip3 install pyspark
#
RUN apt-get install -y libopencv-dev python3-opencv
RUN apt-get install -y python3-tk

RUN echo "+-----------------------+"  && \
	echo "| ===> installing Scala |"  && \
	echo "+-----------------------+"  && \
    DEBIAN_FRONTEND=noninteractive \
            apt-get install -y --allow libjansi-java && \
    curl -sSL $SCALA_TARBALL -o scala.deb && \
    dpkg -i scala.deb && \
    echo "===> Cleaning up..."  && \
    rm -f *.deb

RUN mkdir /workdir
WORKDIR /workdir
RUN echo "+-----------------------+" && \
    echo "| ===> installing Spark |" && \
    echo "+-----------------------+" && \
    DEBIAN_FRONTEND=noninteractive \
    curl -sSL $SPARK_TARBALL -o spark.tgz && \
    tar xvf spark.tgz && \
    echo "===> Cleaning up..." && \
    rm spark.tgz

# TODO Hive, Hadoop, etc?
#
# Jupyter Notebooks kernels: IJava, Almond?
# See https://github.com/SpencerPark/IJava, https://github.com/SpencerPark/IJava#install-from-source
#
WORKDIR /workdir
RUN git clone https://github.com/SpencerPark/IJava.git
WORKDIR /workdir/IJava
RUN pwd
RUN ./gradlew installKernel
#
WORKDIR /workdir
# Coursier (https://get-coursier.io/)
# RUN curl -Lo coursier https://git.io/coursier-cli
# RUN chmod +x coursier
# RUN ./coursier launch --fork almond -- --install
# RUN rm -f coursier
#
# To start: jupyter notebook --ip=0.0.0.0 --port=8080 --allow-root --no-browser
#
# TODO Copy projects, classes, notebooks and resources?
#
RUN echo "alias ll='ls -lisah'" >> $HOME/.bashrc
RUN echo "banner Spark" >> $HOME/.bashrc
RUN echo "git --version" >> $HOME/.bashrc
RUN echo "java -version" >> $HOME/.bashrc
RUN echo "scala -version" >> $HOME/.bashrc
RUN echo "echo -------------------------" >> $HOME/.bashrc
RUN echo "echo From /workdir, cd spark*" >> $HOME/.bashrc
RUN echo "echo Then ./bin/spark-shell " >> $HOME/.bashrc
RUN echo "echo as well as ./bin/pyspark " >> $HOME/.bashrc
RUN echo "echo or ./bin/run-example org.apache.spark.examples.SparkPi" >> $HOME/.bashrc
RUN echo "echo -------------------------" >> $HOME/.bashrc
RUN echo "echo To start Jupyter Notebooks:" >> $HOME/.bashrc
RUN echo "echo jupyter notebook --ip=0.0.0.0 --port=8080 --allow-root --no-browser" >> $HOME/.bashrc
RUN echo "echo -------------------------" >> $HOME/.bashrc
#
RUN echo "To run it, use 'docker run -it --rm -e USER=root -p 8080:8080 oliv-spark:latest /bin/bash'"

# RUN git clone https://github.com/OlivierLD/node.pi.git
# WORKDIR /workdir/node.pi
# RUN npm install
# RUN npm install -g node-inspector

#
# If needed, to unset env vars
#
# ENV http_proxy ""
# ENV https_proxy ""
# ENV ftp_proxy ""
# ENV no_proxy ""

# EXPOSE 9876
CMD ["echo", "Sparkling!"]
