FROM openjdk:14-alpine
COPY build/libs/mn-*-all.jar mn.jar
EXPOSE 8080
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "mn.jar"]