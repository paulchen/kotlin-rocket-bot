FROM openjdk:11-jre-slim
RUN mkdir /app
ADD kotlin-rocket-bot.tar /app
ADD entrypoint.sh /

ENV ROCKETCHAT_HOST=""
ENV ROCKETCHAT_USERNAME=""
ENV ROCKETCHAT_PASSWORD=""

WORKDIR /app/kotlin-rocket-bot
#CMD ["/entrypoint.sh"]
CMD ["bin/kotlin-rocket-bot"]

