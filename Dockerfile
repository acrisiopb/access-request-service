FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw -q dependency:go-offline

COPY src src
RUN ./mvnw -q package -DskipTests

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/access-request-service-0.0.1-SNAPSHOT.jar"]
