package registrationalarm.infra;

import java.util.*;
import org.springframework.http.ResponseEntity;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import org.springframework.http.HttpStatus;

@RestController
@CrossOrigin(origins = "*")
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
        emitter.onError((e) -> {
            System.out.println("SSE error: " + e.getMessage());
        });
        
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                Date now = new Date();
                if (now.getSeconds() <= 2) {
                    String currentTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
                        .format(now);
                    emitter.send(SseEmitter.event()
                        .name("time")
                        .data(currentTime)
                        .reconnectTime(10000));
                }
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }, 0, 1, TimeUnit.SECONDS);
        
        emitter.onCompletion(() -> future.cancel(true));
        emitter.onTimeout(() -> future.cancel(true));
        
        return emitter;
    }

    @GetMapping("/notifications/subscribe")
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        
        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            System.out.println("Notification subscription completed");
        });
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            System.out.println("Notification subscription timeout");
        });
        emitter.onError((e) -> {
            emitters.remove(emitter);
            System.out.println("Notification subscription error: " + e.getMessage());
        });
        
        try {
            emitter.send(SseEmitter.event()
                .name("connect")
                .data("Connected to notification stream")
                .reconnectTime(10000));
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
    public void createReminderNotification(@RequestBody Map<String, Object> notification) {
        // 모든 클라이언트에게 새로운 알림 정보 브로드캐스트
        List<SseEmitter> deadEmitters = new ArrayList<>();
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(Map.of(
                        "type", "NOTIFICATION_ADDED",
                        "notification", notification
                    )));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });
        emitters.removeAll(deadEmitters);
    }
    
}