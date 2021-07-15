FROM openjdk:8-jre-alpine
ADD conbot /conbot
ENTRYPOINT ["/conbot"]

