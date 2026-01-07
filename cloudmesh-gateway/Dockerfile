# Build
FROM gradle:8.14.3-jdk21-corretto AS build
WORKDIR /app
COPY gradle gradle
COPY gradlew gradlew.bat ./
COPY build.gradle.kts settings.gradle.kts ./
RUN gradle dependencies --no-daemon --quiet
COPY src ./src
RUN gradle bootJar --no-daemon --no-build-cache

# Run
FROM eclipse-temurin:21-jre-jammy

RUN groupadd -r -g 1001 appuser && \
    useradd -r -u 1001 -g appuser -m -s /bin/bash appuser

WORKDIR /app
COPY --from=build --chown=appuser:appuser /app/build/libs/*.jar app.jar
COPY --from=build --chown=appuser:appuser /app/src/main/resources/application.yml /app/config/application.yml
RUN mkdir -p /app/logs && chown -R appuser:appuser /app/logs
USER appuser
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

