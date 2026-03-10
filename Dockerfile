FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY src/ ./src/
RUN mkdir -p out && javac -d out src/*.java

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/out ./out
COPY web/ ./web/
ENV PORT=8080
EXPOSE $PORT
CMD java -cp out Main

