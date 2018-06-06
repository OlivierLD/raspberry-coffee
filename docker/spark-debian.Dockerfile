FROM debian

LABEL maintainer="Olivier LeDiouris <olivier@lediouris.net>"

# Will set the proxy in the image. Might be needed to set it at the Docker level too (when building the image).
# ENV http_proxy http://www-proxy.us.oracle.com:80
# ENV https_proxy http://www-proxy.us.oracle.com:80

ENV SCALA_VERSION 2.12.6
ENV SCALA_TARBALL http://www.scala-lang.org/files/archive/scala-$SCALA_VERSION.deb

ENV SPARK_TARBALL http://apache.claz.org/spark/spark-2.3.0/spark-2.3.0-bin-hadoop2.7.tgz

RUN apt-get update
RUN apt-get install -y sysvbanner
RUN apt-get install -y curl git build-essential default-jdk libssl-dev libffi-dev python-dev

RUN echo "+-----------------------+"  && \
		echo "| ===> installing Scala |"  && \
		echo "+-----------------------+"  && \
    DEBIAN_FRONTEND=noninteractive \
            apt-get install -y --force-yes libjansi-java && \
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
		tar xfvz spark.tgz && \
		echo "===> Cleamning up..." && \
		rm spark.tgz

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

# RUN git clone https://github.com/OlivierLD/node.pi.git
# WORKDIR /workdir/node.pi
# RUN npm install
# RUN npm install -g node-inspector

# EXPOSE 9876
CMD ["echo", "Hi there!"]
