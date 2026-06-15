FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/briva.war /app/briva.war
COPY --from=build /app/target/dependency/webapp-runner.jar /app/webapp-runner.jar
EXPOSE 8080
CMD ["java", "-jar", "webapp-runner.jar", "--port", "8080", "briva.war"]