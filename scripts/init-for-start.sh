#!/usr/bin/env bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
export NVM_DIR=$HOME/.nvm;
source $NVM_DIR/nvm.sh;


sdk use java 21.0.6-tem
nvm use v22.16.0

docker compose -f src/main/docker/rabbitmq.yml -f src/main/docker/postgresql.yml up -d
docker compose -f src/main/docker/sonar.yml up -d && \
docker logs -f sonar-token && SONAR_TOKEN=$(docker logs sonar-token) && \
./mvnw -Dspring-boot.run.profiles=local