# Build stage : création du JAR
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src src/

RUN ./mvnw package -DskipTests

# Run stage : image épurée pour lancement
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Ajoute le JAR généré depuis le build précédent
COPY --from=builder /build/target/*.jar app.jar

# Pour éviter problème locale/timezone si besoin :
ENV TZ=Europe/Paris

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
