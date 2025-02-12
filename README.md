# 

## Model
www.msaez.io/#/65217813/storming/registration-alarm

## Before Running Services
### Make sure there is a Kafka server running
```
cd kafka
docker-compose up
```
- Check the Kafka messages:
```
cd infra
docker-compose exec -it kafka /bin/bash
cd /bin
./kafka-console-consumer --bootstrap-server localhost:9092 --topic
```

## Run the backend micro-services
See the README.md files inside the each microservices directory:

- reservation
- notification


## Run API Gateway (Spring Gateway)
```
cd gateway
mvn spring-boot:run
```

## Test by API
- reservation
```
 http :8088/reservations taskId="taskId"userId="userId"title="title"description="description"dueDate="dueDate"
```
- notification
```
 http :8088/notifications notificationId="notificationId"userId="userId"taskId="taskId"dueDate="dueDate"
```


## Run the frontend
```
cd frontend
npm i
npm run serve
```

## Test by UI
Open a browser to localhost:8088

## Required Utilities

- httpie (alternative for curl / POSTMAN) and network utils
```
sudo apt-get update
sudo apt-get install net-tools
sudo apt install iputils-ping
pip install httpie
```

- kubernetes utilities (kubectl)
```
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
```

- aws cli (aws)
```
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

- eksctl 
```
curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin
```


## 예약 알림 테스트

1. 예약 알림 테스트
```
curl -X POST http://localhost:8088/reservations \
-H "Content-Type: application/json" \
-d '{
    "userId": "",
    "title": "test",
    "description": "test",
    "isNow": false,
    "dueDate": "2025-02-12T16:16:00+09:00"
}'
```

2. 실시간 알림 테스트
```
curl -X POST http://localhost:8088/reservations \
-H "Content-Type: application/json" \
-d '{
    "userId": "",
    "title": "test",
    "description": "test",
    "isNow": true,
    "dueDate": null
}'
```

