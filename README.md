# DisorderlyLabs-Microservice-Infrastructure
In-house MicroService Infrastructure for Disorderly Labs

## Prerequisites 
1. JDK 6+
2. Gradle
3. Docker

## Start the services

```
docker-compose up
```

Optionally run the containers in the backgorund with the `-d` flag, and run `docker-compose down` to shut off the containers.

## Run a sample command
`curl -X PUT "http://localhost:7000/app/addToCart?quantity=3&name=Chamber"`

The above call involves all three microservices. First _'App'_ makes a call to _'Inventory'_, to take the item out of inventory (Inventory Database). Then _'App'_ makes a call to _'Cart'_, to add the item to the Cart (Cart Database).         
