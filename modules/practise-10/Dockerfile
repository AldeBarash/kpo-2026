FROM eclipse-temurin:25-jdk AS builder

WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :modules:practise-10:bootJar --no-daemon \
    && find modules/practise-10/build/libs \
        -maxdepth 1 \
        -type f \
        -name '*.jar' \
        ! -name '*-plain.jar' \
        -exec cp '{}' /tmp/application.jar \;

FROM eclipse-temurin:25-jre

WORKDIR /app
COPY --from=builder /tmp/application.jar application.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "application.jar"]
