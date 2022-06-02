FROM debian:bullseye-slim
RUN apt-get update
RUN apt-get -y dist-upgrade
RUN apt-get -y autoremove
RUN apt-get -y purge $(dpkg -l | grep '^rc' | awk '{print $2}')
RUN apt-get clean

ENV JAVA_HOME=/opt/java/openjdk
COPY --from=eclipse-temurin:17-jdk $JAVA_HOME $JAVA_HOME

RUN mkdir /app
ADD kotlin-rocket-bot-latest.tar /app

RUN addgroup --gid 1024 mygroup
RUN adduser --disabled-password --ingroup mygroup myuser
USER myuser

WORKDIR /app/kotlin-rocket-bot-latest
CMD ["bin/kotlin-rocket-bot"]

