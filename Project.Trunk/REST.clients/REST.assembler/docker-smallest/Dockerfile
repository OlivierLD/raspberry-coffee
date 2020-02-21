FROM adoptopenjdk/openjdk11-openj9:jdk-11.0.1.13-alpine-slim
COPY build/libs/small-docker-sensors-1.0-all.jar sensors.jar
EXPOSE 9876
EXPOSE 4000
# RUN echo "JAVA_OPTS=${JAVA_OPTS}"
# CMD java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -jar sensors.jar
CMD java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -Dhttp.port=9876 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:4000 -jar sensors.jar

