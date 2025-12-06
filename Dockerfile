# Build
FROM gradle:8.14.3-jdk21-corretto AS build
WORKDIR /app
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN gradle dependencies --no-daemon
COPY src ./src
COPY cloudmesh-aws ./cloudmesh-aws
COPY cloudmesh-gcp ./cloudmesh-gcp
COPY cloudmesh-azure ./cloudmesh-azure
COPY cloudmesh-digital-ocean ./cloudmesh-digital-ocean
RUN gradle bootJar --no-daemon

# Run
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
