FROM eclipse-temurin:17-jdk
RUN mkdir /app
ADD kotlin-rocket-bot-latest.tar /app

RUN addgroup --gid 1024 mygroup
RUN adduser --disabled-password --ingroup mygroup myuser
USER myuser

WORKDIR /app/kotlin-rocket-bot-latest
CMD ["bin/kotlin-rocket-bot"]

