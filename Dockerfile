# Multi-stage build for Java Spring Boot application
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

# Set the working directory
WORKDIR /app

# Copy Maven files for dependency caching
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Production stage with optimized JRE
FROM eclipse-temurin:21-jre-alpine AS production

# Install tzdata for timezone support
RUN apt-get update && apt-get install -y tzdata && rm -rf /var/lib/apt/lists/*

# Set timezone to Brazil
ENV TZ=America/Sao_Paulo

# Create a non-root user for security
RUN groupadd -r airtrack && useradd -r -g airtrack airtrack

# Set the working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R airtrack:airtrack /app
USER airtrack

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM optimization for containers with Java 21
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=80.0", \
    "-XX:+UseG1GC", \
    "-XX:+UseStringDeduplication", \
    "-XX:+OptimizeStringConcat", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]