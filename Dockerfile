# Multi-stage build
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY mvnw.cmd .
COPY pom.xml .
COPY .mvn .mvn

# Make the Maven wrapper executable and download dependencies with retries
RUN chmod +x ./mvnw && \
    for i in 1 2 3; do \
        ./mvnw dependency:resolve -B && break || \
        if [ $i -lt 3 ]; then \
            echo "Retry $i/3..." && \
            sleep 5; \
        else \
            exit 1; \
        fi \
    done

# Copy source code
COPY src src

# Build argument to control test execution
ARG SKIP_TESTS=false

# Build the application (with or without tests based on SKIP_TESTS arg)
RUN if [ "$SKIP_TESTS" = "true" ] ; then \
      echo "Building without tests..." && ./mvnw clean package -DskipTests ; \
    else \
      echo "Building with tests..." && ./mvnw clean package ; \
    fi

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/war-backend-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
