# build the application
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# build application (creates a WAR file)
RUN mvn clean package -DskipTests

# rnun the application
FROM eclipse-temurin:21-jre

WORKDIR /app

# copy the built WAR/JAR from the builder stage
COPY --from=builder /app/target/*.war app.war

# expose the port that Spring Boot runs on
EXPOSE 8080

# set entrypoint to run the application
ENTRYPOINT ["java", "-jar", "app.war"]
