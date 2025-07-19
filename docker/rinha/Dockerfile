FROM maven:3-amazoncorretto-21-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

RUN useradd --create-home appuser
USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-XX:+UseZGC","-XX:+UseStringDeduplication","-XX:+OptimizeStringConcat","-jar","/app/app.jar"]
