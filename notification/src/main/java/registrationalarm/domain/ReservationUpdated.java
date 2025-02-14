package registrationalarm.domain;

import java.util.*;
import lombok.*;
import registrationalarm.domain.*;
import registrationalarm.infra.AbstractEvent;

@Data
@ToString
public class ReservationUpdated extends AbstractEvent {

    private String taskId;
    private List<String> targetUserIds;
    private String title;
    private String description;
    private Date dueDate;
}
