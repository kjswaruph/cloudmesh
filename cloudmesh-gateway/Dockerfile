# Build
FROM gradle:8.14.3-jdk21-corretto AS build
WORKDIR /app
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts gradlew* ./
RUN gradle dependencies --no-daemon
COPY src ./src
RUN gradle bootJar --no-daemon

# Run
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

