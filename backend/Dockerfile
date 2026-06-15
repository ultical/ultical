FROM maven:3-amazoncorretto-8 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/target/ultical-backend.jar ./ultical-backend.jar
COPY src/main/resources ./src/main/resources/

# Runway reads the exposed port and the (numeric) user from the image metadata.
# The application HTTP connector listens on 8765 by default (override with the
# PORT env var, but keep this EXPOSE in sync if you do). 65534 == "nobody".
EXPOSE 8765
USER 65534

# Logs go to stdout (see default.docker.yaml); the database engine + DSN are
# chosen via DB_DRIVER/DB_URL/DB_USER/DB_PASSWORD env vars at run time.
CMD ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", \
     "-jar", "ultical-backend.jar", "server", "/app/src/main/resources/default.docker.yaml"]
