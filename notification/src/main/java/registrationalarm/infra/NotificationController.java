package registrationalarm.infra;

import java.util.Optional;
import java.util.List;
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
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;
import java.text.SimpleDateFormat;

//<<< Clean Arch / Inbound Adaptor

@RestController
@CrossOrigin(origins = "*")
@Transactional
public class NotificationController {

    @Autowired
    NotificationRepository notificationRepository;

    // 전역 스케줄러로 변경
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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
}
//>>> Clean Arch / Inbound Adaptor
