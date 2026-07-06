# ---------- build stage ----------
# Full JDK to compile; matches the Gradle toolchain (Java 17).
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace

# Copy the wrapper + build config first so the dependency download becomes a cached layer
# that only re-runs when the build files change (not on every source edit).
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
RUN chmod +x ./gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# Now the source, then build the executable (fat) jar. bootJar does NOT run tests.
COPY src src
RUN ./gradlew --no-daemon clean bootJar

# ---------- runtime stage ----------
# Slim JRE only — no compiler, smaller attack surface.
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# Run as a non-root user (good practice; some clusters enforce it).
RUN groupadd --system app && useradd --system --gid app --home /app app

COPY --from=build /workspace/build/libs/*.jar app.jar
USER app
EXPOSE 8080

# `exec` makes the JVM PID 1 so it receives SIGTERM directly — required for graceful
# shutdown during a K8s rolling update.
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
