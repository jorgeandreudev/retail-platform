FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . /workspace
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/products-service-*.jar /app/app.jar
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=3s --retries=5 CMD curl -fsS http://localhost:8081/actuator/health || exit 1
ENTRYPOINT ["java","-jar","/app/app.jar"]