package sns.pinocchio.domain.notification;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNotification is a Querydsl query type for Notification
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotification extends EntityPathBase<Notification> {

    private static final long serialVersionUID = -1133253772L;

    public static final QNotification notification = new QNotification("notification");

    public final BooleanPath commentAlert = createBoolean("commentAlert");

    public final BooleanPath followAlert = createBoolean("followAlert");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath likeAlert = createBoolean("likeAlert");

    public final BooleanPath mentionAlert = createBoolean("mentionAlert");

    public final BooleanPath messageAlert = createBoolean("messageAlert");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final StringPath userId = createString("userId");

    public QNotification(String variable) {
        super(Notification.class, forVariable(variable));
    }

    public QNotification(Path<? extends Notification> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNotification(PathMetadata metadata) {
        super(Notification.class, metadata);
    }

}

