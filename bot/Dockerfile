FROM debian:bullseye-slim
RUN apt-get update
RUN apt-get -y dist-upgrade
RUN apt-get -y autoremove
RUN apt-get -y purge $(dpkg -l | grep '^rc' | awk '{print $2}')
RUN apt-get clean

ENV JAVA_HOME=/opt/java/openjdk
COPY --from=eclipse-temurin:17-jdk $JAVA_HOME $JAVA_HOME

RUN mkdir /app
ADD bot-latest.tar /app

RUN addgroup --gid 32001 mygroup
RUN adduser --disabled-password --ingroup mygroup --uid 32001 myuser
USER myuser

WORKDIR /app/bot-latest
CMD ["bin/bot"]

