# syntax=docker/dockerfile:1
FROM openjdk:17-buster
RUN apt-get update
RUN apt-get install -y  ffmpeg imagemagick
WORKDIR /app
COPY ./build/libs/legacy-0.0.1-SNAPSHOT.jar ./app.jar
CMD ["java", "-jar", "app.jar"]
EXPOSE 8080
