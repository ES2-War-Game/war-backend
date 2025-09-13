FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/war-backend-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 3333

ENTRYPOINT ["java", "-jar", "app.jar"]
