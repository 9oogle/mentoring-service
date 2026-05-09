FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
RUN mkdir -p ssl logs
EXPOSE 9006
ENTRYPOINT ["java", "-jar", "app.jar"]
