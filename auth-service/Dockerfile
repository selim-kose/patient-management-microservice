# Dockerfile for the Patient Service application
# Use the official Maven image to build the application
FROM maven:3.9.9-eclipse-temurin-21 AS builder
LABEL authors="selim"

#Create a directory for the application in the container
WORKDIR /app
# Copy the pom.xml file to the container in the /app directory
COPY pom.xml .
# Download the dependencies with option go offline, it will cache the dependencies and speed up the build process
RUN mvn dependency:go-offline -B
# Copy the source code to the container in the /app directory
COPY src ./src
# Build the application using Mave and skip tests. It will create a jar file in the target directory
RUN mvn clean package -DskipTests

# Use the official OpenJDK image to run the application
FROM openjdk:21-jdk AS runner
# Create a directory for the application in the container
WORKDIR /app
# Copy the jar file from the builder stage to the runner stage
COPY --from=builder /app/target/auth-service-0.0.1-SNAPSHOT.jar ./auth-service.jar

# Expose the port on which the application will run
EXPOSE 4005

# Set the entry point for the container to run the application
ENTRYPOINT ["java", "-jar", "auth-service.jar"]