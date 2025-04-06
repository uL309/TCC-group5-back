FROM openjdk:11-jre-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project files
COPY pom.xml .
COPY src ./src

# Install Maven and build the application
RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests

# Copy the built JAR file to the container
COPY target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Set the entry point for the application
ENTRYPOINT ["java", "-jar", "app.jar"]