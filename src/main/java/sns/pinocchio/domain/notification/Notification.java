package sns.pinocchio.domain.notification;

import jakarta.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;

@Document(collection = "notifications")
public class Notification {

  @Id private String id;

  private String userId;

  private boolean message;

  private boolean like;

  private boolean comment;

  private boolean follow;

  private boolean mention;

  private ZonedDateTime updatedAt;
}
