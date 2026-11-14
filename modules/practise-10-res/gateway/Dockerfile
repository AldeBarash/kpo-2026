FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :modules:practise-10-res:gateway:bootJar --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=builder /workspace/modules/practise-10-res/gateway/build/libs/gateway.jar application.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "application.jar"]
