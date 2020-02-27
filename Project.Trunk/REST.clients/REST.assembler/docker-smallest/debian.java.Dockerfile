# FROM debian:stretch
# FROM resin/raspberrypi3-debian:latest
FROM openjdk:8-jre-slim

LABEL maintainer="Olivier LeDiouris <olivier@lediouris.net>"

# ENV http_proxy http://www-proxy.us.oracle.com:80
# ENV https_proxy http://www-proxy.us.oracle.com:80

# RUN apt-get update
# RUN apt-get install -y default-jdk
# RUN apt-get install -y oracle-java9-jdk

COPY build/libs/small-docker-sensors-1.0-all.jar sensors.jar

EXPOSE 9876
# EXPOSE 4000
# RUN echo "JAVA_OPTS=${JAVA_OPTS}"
CMD java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -Dhttp.port=9876 -jar sensors.jar
# CMD java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -Dhttp.port=9876 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:4000 -jar sensors.jar

