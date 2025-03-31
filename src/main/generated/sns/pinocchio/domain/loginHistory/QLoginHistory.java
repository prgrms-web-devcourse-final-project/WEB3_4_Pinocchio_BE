package sns.pinocchio.domain.loginHistory;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QLoginHistory is a Querydsl query type for LoginHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLoginHistory extends EntityPathBase<LoginHistory> {

    private static final long serialVersionUID = 699886452L;

    public static final QLoginHistory loginHistory = new QLoginHistory("loginHistory");

    public final StringPath loginDevice = createString("loginDevice");

    public final NumberPath<Long> loginHistoryId = createNumber("loginHistoryId", Long.class);

    public final StringPath loginIp = createString("loginIp");

    public final DateTimePath<java.time.LocalDateTime> loginTime = createDateTime("loginTime", java.time.LocalDateTime.class);

    public final StringPath userAgent = createString("userAgent");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QLoginHistory(String variable) {
        super(LoginHistory.class, forVariable(variable));
    }

    public QLoginHistory(Path<? extends LoginHistory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLoginHistory(PathMetadata metadata) {
        super(LoginHistory.class, metadata);
    }

}

