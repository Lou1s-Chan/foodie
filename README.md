# Welcome to Team Foodies! 🍕🍝

## But what is Foodies? 

Foodies is a distributed system that uses Akka Actors to carry out the necessary functionalities of a food delivery system. It has 5 core services, which are 
- user 
- order
- restaurant
- payment
- delivery

Each member of our group was responsible for one of the services. A user can log in, choose a restaurant by its ID and insert menu items into their basket, also by their IDs. Then, they can proceed with the order. Once the order is finalized, they can choose to pay by Card or Cash. This information is verified by payment service and upon successful transaction, the restaurant and the delivery services are notified of the order.

Our front-end dashboard receives live updates from the server and shows the health and status of the entire system in real-time! This was done thanks to SSE technology. 

<img src = '/dashboard-screenshot.png' alt = "cover" />

## How to run the system:

1. Go into the project folder and type:
```
mvn clean install
```
```
docker compose up --build
```
this will run 4 of the service containers (excluding user).

2. Next, go to user service folder to run this service:
```
cd userService
```
```
docker buildx build -t 'user-service' .
```
If you encounter network error, type:
```
docker network create foodie_foodie-network
```
then run the user service:
```
docker run -it -p 2552:2552 -p 8082:8082 --network foodie_foodie-network user-service
```

3. Voila! 

## Instruction on Kubernetes deployment
0. You need to make sure you have appropriate Kubectl installation and create Kubernetes cluster with Kind/Kubeadm or any other similar tools.

1. Going to the userService folder.
Change all the 'order-system@order-service' in both Main class and UserActor class to 'order-system@localhost'

2. Go to the project root folder and type:
```
mvn package
```
```
docker compose build
```
All the images required for Kubernetes deployment will be made in this step.

3. Go to the folder 'k8s-config'.
```
kubectl apply -f *.yaml
```

4. All other thing should just like dockerization the user service, but do not need to specify a network for it.

5. Enjoy~

## 🎥 Watch our video [here](https://drive.google.com/file/d/1STfa-P64WnnOVKS1Gz2iPkgbAtfGayyi/view?usp=sharing) 🌟
## 📖 Read our report [here]() 🌠




