FROM gradle:jdk8-alpine AS builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon 

FROM openjdk:8-jre-slim
EXPOSE 8080
RUN mkdir /app
COPY --from=builder /home/gradle/src/build/libs/*.jar /app/spring-boot-application.jar
# overwrite application.properties for spring.datasource.url: localhost --> dmt-postgres-db (DB in docker container)
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Dspring.datasource.url=jdbc:postgresql://dmt-postgres-db:5432/dmt", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/spring-boot-application.jar"]