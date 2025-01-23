package registrationalarm.domain;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import registrationalarm.domain.*;

//<<< PoEAA / Repository
@RepositoryRestResource(
    collectionResourceRel = "notifications",
    path = "notifications"
)
public interface NotificationRepository
    extends PagingAndSortingRepository<Notification, String> {}
