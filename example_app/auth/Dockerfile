FROM gradle:9.3.1-jdk25-alpine AS build
WORKDIR /app
COPY . .
RUN gradle --build-cache assemble --no-daemon

FROM amazoncorretto:25.0.2
WORKDIR /app
COPY --from=build /app/build/libs/example-ecommerce-auth-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

