FROM maven:3-amazoncorretto-8 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn verify --fail-never
COPY src ./src
RUN mvn clean package 
# -DskipTests

# requires the database to be up and running
RUN mvn -X liquibase:update

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/target/ultical-backend.jar ./ultical-backend.jar
COPY src/main/resources ./src/main/resources/

RUN apk add --no-cache bash
RUN apk add --no-cache maven

# Copy the Maven settings and dependencies needed for Liquibase
COPY --from=build /root/.m2 /root/.m2
COPY pom.xml .

CMD ["java", "-jar", "ultical-backend.jar", "server", "/app/src/main/resources/default.docker.yaml"]
