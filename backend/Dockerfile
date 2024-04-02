FROM maven:3-amazoncorretto-8 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/target/ultical-backend.jar ./ultical-backend.jar
COPY src/main/resources ./src/main/resources/
CMD ["java", "-jar", "ultical-backend.jar", "server", "/app/src/main/resources/default.docker.yaml"]
