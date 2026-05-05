# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/chatbot-0.0.1-SNAPSHOT.jar app.jar

# Render usually uses port 10000 by default, but we'll use an env var
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]