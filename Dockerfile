FROM eclipse-temurin:25-jdk AS jdk

COPY . /opt/kotlin-rocket-bot
RUN apt-get update && \
    apt-get install -y --no-install-recommends git && \
    cd /opt && \
    git clone https://github.com/paulchen/kotlin-rocket-lib.git && \
    cd kotlin-rocket-lib && \
    mkdir kotlin-rocket-lib && \
    ./gradlew publishToMavenLocal && \
    cd ../kotlin-rocket-bot && \
    ./gradlew installDist

FROM debian:trixie-slim
RUN apt-get update && \
    apt-get -y dist-upgrade && \
    apt-get -y install --no-install-recommends python3-requests psmisc adduser && \
    apt-get -y autoremove && \
    apt-get -y purge $(dpkg -l | grep '^rc' | awk '{print $2}') && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/opt/java/openjdk
COPY --from=eclipse-temurin:25-jre $JAVA_HOME $JAVA_HOME

RUN mkdir /app
COPY --from=jdk /opt/kotlin-rocket-bot/bot/build/install/bot/ /app/kotlin-rocket-bot/
COPY misc/check_bot.py misc/docker-entrypoint.sh /opt

RUN addgroup --gid 32001 mygroup && \
    adduser --disabled-password --ingroup mygroup --uid 32001 myuser
USER myuser

HEALTHCHECK --interval=1m --timeout=10s CMD WEBSERVICE_PORT=8082 /opt/check_bot.py || exit 1 

CMD ["/opt/docker-entrypoint.sh"]

