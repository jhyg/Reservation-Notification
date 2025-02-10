package registrationalarm.infra;

import java.util.*;
import org.springframework.http.ResponseEntity;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import registrationalarm.domain.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;
import java.text.SimpleDateFormat;

@RestController
@CrossOrigin(origins = "*")
@Transactional
public class NotificationController {

    @Autowired
    NotificationRepository notificationRepository;

    // 전역 스케줄러
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    // 실시간 알림을 위한 SseEmitter 목록
    private static final List<SseEmitter> emitters = new ArrayList<>();

    @GetMapping("/notifications/stream")
    public SseEmitter streamTime() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        emitter.onCompletion(() -> {
            System.out.println("SSE completed");
        });
        emitter.onTimeout(() -> {
            System.out.println("SSE timeout");
        });
        
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                Date now = new Date();
                // 현재 초가 0~2초 사이일 때만 알림을 보냄
                if (now.getSeconds() <= 2) {
                    String currentTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
                        .format(now);
                    emitter.send(SseEmitter.event()
                        .name("time")
                        .data(currentTime));
                }
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }, 0, 1, TimeUnit.SECONDS);  // 1초마다 체크
        
        // emitter가 완료되면 해당 작업만 취소
        emitter.onCompletion(() -> future.cancel(true));
        emitter.onTimeout(() -> future.cancel(true));
        
        return emitter;
    }

    @GetMapping("/notifications/subscribe")
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        
        // 연결이 종료되면 emitter 제거
        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            System.out.println("Notification subscription completed");
        });
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            System.out.println("Notification subscription timeout");
        });
        
        // 연결 시 테스트 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                .name("connect")
                .data("Connected to notification stream"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        
        return emitter;
    }

    @PostMapping("/notifications/broadcast")
    public void broadcast(@RequestBody Map<String, String> notification) {
        List<SseEmitter> deadEmitters = new ArrayList<>();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notification));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });

        // 죽은 emitter들 제거
        emitters.removeAll(deadEmitters);
    }

    @PostMapping("/notifications/reminder") 
    public ResponseEntity<Notification> createReminderNotification(@RequestBody Notification notification) {
        Notification savedNotification = notificationRepository.save(notification);
        
        // 모든 클라이언트에게 새로운 알림 정보 브로드캐스트
        List<SseEmitter> deadEmitters = new ArrayList<>();
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(Map.of(
                        "type", "NOTIFICATION_ADDED",
                        "notification", savedNotification
                    )));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });
        emitters.removeAll(deadEmitters);
        
        return ResponseEntity.ok(savedNotification);
    }

    // 현재 연결된 클라이언트 수 확인용 (디버깅/모니터링용)
    @GetMapping("/notifications/connections")
    public Map<String, Integer> getConnectionCount() {
        return Collections.singletonMap("connections", emitters.size());
    }
}