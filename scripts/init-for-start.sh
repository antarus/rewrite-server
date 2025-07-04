#!/usr/bin/env bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
export NVM_DIR=$HOME/.nvm;
source $NVM_DIR/nvm.sh;

sdk env
nvm use v22.16.0

export JAVA_HOME=$(sdk home java 21.0.4-tem)
#docker compose -f src/main/docker/rabbitmq.yml up -d
docker compose -f src/main/docker/sonar.yml up -d && \
docker logs sonar-token && SONAR_TOKEN=$(docker logs sonar-token) && \
./mvnw -Dspring-boot.run.profiles=local