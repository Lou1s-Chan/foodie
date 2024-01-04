FROM openjdk:8-jdk

WORKDIR /app

COPY /target/user-service-2.0-allinone.jar app.jar

EXPOSE 2552
EXPOSE 8082

CMD ["java","-jar","app.jar"]