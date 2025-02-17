package registrationalarm.infra;

import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import registrationalarm.domain.*;
import java.util.Map;
import java.util.HashMap;
import org.springframework.web.client.RestTemplate;

//<<< Clean Arch / Inbound Adaptor

@RestController
// @RequestMapping(value="/reservations")
@Transactional
public class ReservationController {

    @Autowired
    ReservationRepository reservationRepository;

    @PostMapping("/reservations")
    public Reservation createReservation(@RequestBody Reservation reservation) {
        if (reservation.isNow() || reservation.getDueDate() == null) {
            // 즉시 알림 발송
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("title", reservation.getTitle());
            notificationData.put("targetUserIds", reservation.getTargetUserIds());
            notificationData.put("description", reservation.getDescription());

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForEntity("http://localhost:8083/notifications/broadcast", 
                                     notificationData, Object.class);
            return null;
        } else {
            // 예약 알림 처리
            String uuid = UUID.randomUUID().toString();
            reservation.setTaskId(uuid);
            
            try {
                Reservation savedReservation = reservationRepository.save(reservation);

                // notification 데이터 생성 및 전송
                Map<String, Object> notificationData = new HashMap<>();
                notificationData.put("taskId", savedReservation.getTaskId());
                notificationData.put("targetUserIds", savedReservation.getTargetUserIds());
                notificationData.put("dueDate", savedReservation.getDueDate());
                notificationData.put("title", savedReservation.getTitle());
                notificationData.put("description", savedReservation.getDescription());

                RestTemplate restTemplate = new RestTemplate();
                restTemplate.postForEntity("http://localhost:8083/notifications/reminder", 
                                         notificationData, Object.class);
                
                return savedReservation;
            } catch (Exception e) {
                throw new RuntimeException("Failed to process reservation: " + e.getMessage());
            }
        }
    }

    @GetMapping("/reservations")
    public Iterable<Reservation> getReservations() {
        return reservationRepository.findAll();
    }
}
//>>> Clean Arch / Inbound Adaptor