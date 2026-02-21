FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/syswatch.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]