FROM openjdk:14-alpine
COPY build/libs/meta-server-*-all.jar meta-server.jar
EXPOSE 8080
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "meta-server.jar"]