FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY src src

RUN mvn -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/target/doari-*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
