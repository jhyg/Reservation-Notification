package registrationalarm.domain;

import java.util.*;
import lombok.*;
import registrationalarm.domain.*;
import registrationalarm.infra.AbstractEvent;

@Data
@ToString
public class ReservationDeleted extends AbstractEvent {

    private String taskId;
}
