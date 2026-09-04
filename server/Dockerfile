# syntax=docker/dockerfile:1.7
# BuildKit cache keeps ~/.m2/repository across builds so deps are not re-downloaded every time.

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY maven-settings.xml /root/.m2/settings.xml
COPY pom.xml .

# Warm dependency cache when pom.xml changes only (src not needed yet).
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -q -B -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -q -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
ENV TZ=Asia/Shanghai
RUN mkdir -p /app/uploads
COPY --from=build /app/target/academy-server-1.0.0.jar app.jar
EXPOSE 80
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
