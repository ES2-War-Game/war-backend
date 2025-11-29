FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

COPY mvnw .
COPY mvnw.cmd .
COPY pom.xml .
COPY .mvn .mvn

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

COPY src src

# Force skipping tests during image build to avoid requiring a Docker engine
ARG SKIP_TESTS=true

RUN echo "SKIP_TESTS=${SKIP_TESTS}" && \
    ./mvnw -B -DskipTests=true -Dmaven.test.skip=true -DskipITs=true clean package

FROM eclipse-temurin:17-jre-alpine

ENV PORT 8080

WORKDIR /app

COPY --from=build /app/target/war-backend-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE ${PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]