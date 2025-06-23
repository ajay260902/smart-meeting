# Use a base image with Java 17
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the built JAR file into the container
COPY target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
