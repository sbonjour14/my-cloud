FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY . .
RUN chmod +x mvnw || true
EXPOSE 8080
CMD ["./mvnw", "spring-boot:run"]
