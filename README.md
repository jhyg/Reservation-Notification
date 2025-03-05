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
    "targetUserIds": ["target_all_users"],
    "title": "예약 알림",
    "description": "예약 알림 설명",
    "isNow": false,
    "dueDate": "2025-02-17T14:49:00+09:00"
}'
```

2. 실시간 알림 테스트
```
curl -X POST http://localhost:8088/reservations \
-H "Content-Type: application/json" \
-d '{
    "targetUserIds": ["target_all_users"],
    "title": "실시간 알림",
    "description": "실시간 알림 설명",
    "isNow": true,
    "dueDate": null
}'
```

## PBC 적용 방법
1. https://github.com/kyusooK/Cluster-distribute-rule/blob/main/README.md 

2. 위 설정 방법을 확인한 후 gateway에 등록한 backend 명칭으로 ReservationController.java 파일의 기존 2개 api 호출 주소 변경
    1. root gateway에 backend 명칭을 http://notification:8080 라고 등록한 경우 기존의 http://localhost:8083 을 http://notification:8080 로 변경
    ```
    restTemplate.postForEntity("http://notification:8080/notifications/broadcast", notificationData, Object.class);
    restTemplate.postForEntity("http://notification:8080/notifications/broadcast", notificationData, Object.class);
    ```

