package registrationalarm.infra;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.naming.NameParser;
import javax.naming.NameParser;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import registrationalarm.config.kafka.KafkaProcessor;
import registrationalarm.domain.*;

//<<< Clean Arch / Inbound Adaptor
@Service
@Transactional
public class PolicyHandler {

    @Autowired
    NotificationRepository notificationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {}

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='ReservationCreated'"
    )
    public void wheneverReservationCreated_ScheduleNotification(
        @Payload ReservationCreated reservationCreated
    ) {
        ReservationCreated event = reservationCreated;
        System.out.println(
            "\n\n##### listener ScheduleNotification : " +
            reservationCreated +
            "\n\n"
        );

        // Sample Logic //
        Notification.scheduleNotification(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='ReservationUpdated'"
    )
    public void wheneverReservationUpdated_ScheduleNotification(
        @Payload ReservationUpdated reservationUpdated
    ) {
        ReservationUpdated event = reservationUpdated;
        System.out.println(
            "\n\n##### listener ScheduleNotification : " +
            reservationUpdated +
            "\n\n"
        );

        // Sample Logic //
        Notification.scheduleNotification(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='ReservationDeleted'"
    )
    public void wheneverReservationDeleted_DeleteNotification(
        @Payload ReservationDeleted reservationDeleted
    ) {
        ReservationDeleted event = reservationDeleted;
        System.out.println(
            "\n\n##### listener DeleteNotification : " +
            reservationDeleted +
            "\n\n"
        );

        // Sample Logic //
        Notification.deleteNotification(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='ReservationCreated'"
    )
    public void wheneverReservationCreated_SendNotification(
        @Payload ReservationCreated reservationCreated
    ) {
        ReservationCreated event = reservationCreated;
        System.out.println(
            "\n\n##### listener SendNotification : " +
            reservationCreated +
            "\n\n"
        );

        // Sample Logic //
        Notification.sendNotification(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='ReservationUpdated'"
    )
    public void wheneverReservationUpdated_SendNotification(
        @Payload ReservationUpdated reservationUpdated
    ) {
        ReservationUpdated event = reservationUpdated;
        System.out.println(
            "\n\n##### listener SendNotification : " +
            reservationUpdated +
            "\n\n"
        );

        // Sample Logic //
        Notification.sendNotification(event);
    }
}
//>>> Clean Arch / Inbound Adaptor
