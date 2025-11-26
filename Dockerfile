FROM eclipse-temurin:17-jdk

# Instalar Maven
RUN apt-get update && apt-get install -y maven

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

EXPOSE 8081

CMD ["java", "-jar", "target/loginSAD-0.0.1-SNAPSHOT.jar"]
