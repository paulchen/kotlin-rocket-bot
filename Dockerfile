FROM openjdk:17-jdk-slim
RUN mkdir /app
ADD kotlin-rocket-bot.tar /app

WORKDIR /app/kotlin-rocket-bot
CMD ["bin/kotlin-rocket-bot"]

