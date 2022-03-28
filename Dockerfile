FROM openjdk:17-jdk-slim
RUN mkdir /app
ADD kotlin-rocket-bot-latest.tar /app

RUN addgroup --gid 1024 mygroup
RUN adduser --disabled-password --ingroup mygroup myuser
USER myuser

WORKDIR /app/kotlin-rocket-bot
CMD ["bin/kotlin-rocket-bot"]

