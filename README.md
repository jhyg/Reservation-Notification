## Model
www.msaez.io/#/65217813/storming/registration-alarm

## Before Running Services
### Make sure there is a Kafka server running
```
cd infra
docker-compose up
```

## Run the backend micro-services
```
cd reservation
mvn clean spring-boot:run

cd notification
mvn clean spring-boot:run
```


## Run API Gateway (Spring Gateway)
```
cd gateway
mvn clean spring-boot:run
```

## Run the frontend
```
cd frontend
npm i
npm run serve
```

## Test by UI
Open a browser to http://localhost:8088/#/

## 예약 알림 테스트

1. 예약 알림 테스트
```
curl -X POST http://localhost:8088/reservations \
-H "Content-Type: application/json" \
-d '{
    "targetUserIds": [],
    "title": "예약 알림3",
    "description": "test",
    "isNow": false,
    "dueDate": "2025-02-14T14:29:00+09:00"
}'
```

2. 실시간 알림 테스트
```
curl -X POST http://localhost:8088/reservations \
-H "Content-Type: application/json" \
-d '{
    "targetUserIds": ["jhyg"],
    "title": "실시간 알림",
    "description": "test",
    "isNow": true,
    "dueDate": null
}'
```

