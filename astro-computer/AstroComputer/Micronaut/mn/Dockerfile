FROM openjdk:14-alpine
# For Raspberry Pi, comment the above, un-comment the 4 next lines
# >> Another option would be SDKMAN
# FROM debian:buster
# RUN apt-get update
# RUN apt-get install -y curl git build-essential default-jdk procps net-tools wget
# or
# RUN apt-get install -y openjdk-11-jdk
# RUN java -version
#
COPY build/libs/mn-*-all.jar mn.jar
EXPOSE 8081
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "mn.jar"]
