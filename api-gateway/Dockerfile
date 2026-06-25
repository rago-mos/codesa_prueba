# Build stage
FROM gradle:8.13-jdk21-alpine AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
# Por defecto usa el profile 'docker' para comunicarse con servicios por nombre
CMD ["java", "-jar", "app.jar"]
