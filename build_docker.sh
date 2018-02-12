#!/bin/bash

# set -e

echo Stopping all docker containers...
docker stop $(docker ps -a -q)
echo Removing all docker containers...
docker rm $(docker ps -a -q)

echo Building App container...
cd app
./gradlew build docker
if [[ $? -ne 0 ]]; then
    exit 1
fi
echo Building Cart container...
cd ../cart
./gradlew build docker
if [[ $? -ne 0 ]]; then
    exit 1
fi
echo Building Inventory container...
cd ../inventory
./gradlew build docker
if [[ $? -ne 0 ]]; then
    exit 1
fi

cd ..

echo Starting Inventory
docker run -p 7001:8080 --net=mynet --ip=10.0.0.21 -d com.disorderlylabs/inventory
echo Starting Cart
docker run -p 7002:8080 --net=mynet --ip=10.0.0.22 -d com.disorderlylabs/cart
echo Starting App
docker run -p 7000:8080 --net=mynet -e inventory_ip=10.0.0.21:8080 -e cart_ip=10.0.0.22:8080 -d com.disorderlylabs/app

sleep 5
echo Done.  Run commands using curl -X PUT [options]