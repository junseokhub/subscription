FROM docker.io/library/eclipse-temurin:21 as deps

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

FROM docker.io/library/eclipse-temurin:21 as builder

WORKDIR /app

COPY --from=deps /app ./

COPY src src

RUN ./gradlew bootJar --no-daemon -x test

FROM docker.io/library/eclipse-temurin:21 as runner
WORKDIR /app

RUN addgroup --system --gid 1001 spring && adduser --system --uid 1001 springuser

COPY --from=builder /app/build/libs/*.jar app.jar
RUN chown springuser:spring /app/app.jar
USER springuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]