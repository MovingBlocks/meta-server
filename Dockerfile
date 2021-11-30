FROM gradle:6.6.1-jdk11 as  builder
CMD mkdir /build
COPY . /build
WORKDIR /build
RUN gradle shadowJar

FROM openjdk:14-alpine
COPY --from=builder build/build/libs/meta-server-*-all.jar meta-server.jar
EXPOSE 80
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "meta-server.jar"]