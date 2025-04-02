package sns.pinocchio.domain.blockedUser;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBlockedUser is a Querydsl query type for BlockedUser
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBlockedUser extends EntityPathBase<BlockedUser> {

    private static final long serialVersionUID = -1260823578L;

    public static final QBlockedUser blockedUser = new QBlockedUser("blockedUser");

    public final NumberPath<Long> blockedUserId = createNumber("blockedUserId", Long.class);

    public final NumberPath<Long> blockerUserId = createNumber("blockerUserId", Long.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public QBlockedUser(String variable) {
        super(BlockedUser.class, forVariable(variable));
    }

    public QBlockedUser(Path<? extends BlockedUser> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBlockedUser(PathMetadata metadata) {
        super(BlockedUser.class, metadata);
    }

}

