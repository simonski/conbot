FROM adoptopenjdk/openjdk11:alpine-jre
# FROM openjdk:8-jre-alpine
ADD conbot /conbot
ENTRYPOINT ["/conbot"]

