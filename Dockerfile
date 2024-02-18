FROM debian:bookworm-slim
RUN apt-get update
RUN apt-get -y dist-upgrade
RUN apt-get -y install --no-install-recommends python3-requests
RUN apt-get -y autoremove
RUN apt-get -y purge $(dpkg -l | grep '^rc' | awk '{print $2}')
RUN apt-get clean

ENV JAVA_HOME=/opt/java/openjdk
COPY --from=eclipse-temurin:21-jdk $JAVA_HOME $JAVA_HOME

RUN mkdir /app
ADD bot/build/distributions/bot-latest.tar /app
COPY misc/check_bot.py /opt

RUN addgroup --gid 32001 mygroup
RUN adduser --disabled-password --ingroup mygroup --uid 32001 myuser
USER myuser

HEALTHCHECK --interval=1m --timeout=10s CMD WEBSERVICE_PORT=8082 /opt/check_bot.py || exit 1 

WORKDIR /app/bot-latest
CMD ["bin/bot"]

