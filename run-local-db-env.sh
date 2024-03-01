#!/bin/bash

set -x

# Exit if docker is not installed
if ! [ -x "$(command -v docker)" ]; then
  echo >&2 "Error: $(docker) is not installed."
  exit 1
fi

# Exit if docker-compose is not installed
if ! [ -x "$(command -v docker-compose)" ]; then
  echo >&2 "Error: $(docker-compose) is not installed."
  exit 1
fi

# Exit if maven is not installed
if ! [ -x "$(command -v mvn)" ]; then
  echo >&2 "Error: $(mvn) is not installed."
  exit 1
fi


docker-compose up -d
# make sure the db has been set up before starting quarkus
echo "Waiting 5 seconds for DB to initialise . . ."
sleep 5
export PCC_API_PROFILE=dev
export HRLS_DATABASE_IP=localhost
export HRLS_DATABASE_PORT=3307
export HRLS_DATABASE_NAME=hrlsdb
export HRLS_DATABASE_USERNAME=hrls
export HRLS_DATABASE_PASSWORD=hrls
echo "Starting quarkus pcc-api . .. "
mvn clean install
mvn quarkus:dev -DskipTests=true

