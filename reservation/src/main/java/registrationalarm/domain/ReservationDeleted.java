package registrationalarm.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.*;
import registrationalarm.domain.*;
import registrationalarm.infra.AbstractEvent;

//<<< DDD / Domain Event
@Data
@ToString
public class ReservationDeleted extends AbstractEvent {

    private String taskId;

    public ReservationDeleted(Reservation aggregate) {
        super(aggregate);
    }

    public ReservationDeleted() {
        super();
    }
}
//>>> DDD / Domain Event
