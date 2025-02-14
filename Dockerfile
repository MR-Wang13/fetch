# Stage 1: Build the application using Maven
FROM maven:3.8.5-openjdk-17-slim AS builder
WORKDIR /app

# Copy pom.xml and download dependencies first (caching dependencies)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM openjdk:17-jdk-slim
WORKDIR /app

# Expose the application port (adjust if needed)
EXPOSE 8080

# Copy the jar file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Define the entry point to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
