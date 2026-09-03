FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY maven-settings.xml /root/.m2/settings.xml
COPY pom.xml .
COPY src ./src
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
ENV TZ=Asia/Shanghai
RUN mkdir -p /app/uploads
COPY --from=build /app/target/academy-server-1.0.0.jar app.jar
EXPOSE 80
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
