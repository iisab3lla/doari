FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY usuarios-service/pom.xml usuarios-service/pom.xml
COPY doacoes-service/pom.xml doacoes-service/pom.xml
COPY notificacao-service/pom.xml notificacao-service/pom.xml
COPY usuarios-service/src usuarios-service/src

RUN mvn -pl usuarios-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/usuarios-service/target/usuarios-service-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
