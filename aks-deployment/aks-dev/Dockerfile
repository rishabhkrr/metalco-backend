FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /usr/src/app

# Copy pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy source code
COPY src ./src

# Build without running tests (much faster)
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine

COPY --from=builder /usr/src/app/target/wms-0.0.1-SNAPSHOT.jar /app.jar

EXPOSE 8089

ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "/app.jar"]
