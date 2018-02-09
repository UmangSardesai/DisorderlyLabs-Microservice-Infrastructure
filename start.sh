#!/bin/sh

docker run --net=mynet -d -p 9411:9411 --ip=10.0.0.24 openzipkin/zipkin
docker run --net=mynet -d -p 7001:8080 --ip=10.0.0.22 -e zipkin_ip=10.0.0.24 com.disorderlylabs/cart
docker run --net=mynet -d -p 7002:8080 -e cart_ip=10.0.0.22:8080 -e zipkin_ip=10.0.0.24 --ip=10.0.0.21 com.disorderlylabs/inventory
docker run --net=mynet -d -p 7000:8080 -e inventory_ip=10.0.0.21:8080 -e cart_ip=10.0.0.22:8080 -e zipkin_ip=10.0.0.24 com.disorderlylabs/app

#docker run --net=mynet --name inventory -d -p 7002:8080 com.disorderlylabs/inventory
#docker run --net=mynet --name cart -d -p 7001:8080 com.disorderlylabs/cart
#docker run --net=mynet --name app -d -p 7000:8080 com.disorderlylabs/app
