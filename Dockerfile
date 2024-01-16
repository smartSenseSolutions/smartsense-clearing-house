FROM gradle:8.5-jdk17-alpine AS build
COPY --chown=gradle:gradle . /home/app
WORKDIR /home/app
RUN gradle clean build --no-daemon -i -x test -x javadoc

FROM eclipse-temurin:17-jre-alpine
COPY --from=build /home/app/build/libs/smartSenseCH-0.0.1-SNAPSHOT.jar /usr/local/lib/smartSenseCH.jar
RUN apk update && apk upgrade libssl3 libcrypto3
RUN addgroup -S ssch && adduser -S ssch -G ssch
USER ssch
WORKDIR /usr/local/lib
EXPOSE 8080
ENTRYPOINT ["java","-jar","smartSenseCH.jar"]
