FROM openjdk:11-jre-slim
RUN mkdir /app
ADD kotlin-rocket-bot.tar /app

ENV ROCKETCHAT_HOST=""
ENV ROCKETCHAT_USERNAME=""
ENV ROCKETCHAT_PASSWORD=""

WORKDIR /app/kotlin-rocket-bot
CMD ["bin/kotlin-rocket-bot"]

