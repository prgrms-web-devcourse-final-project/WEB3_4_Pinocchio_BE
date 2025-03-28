package sns.pinocchio.infrastructure.persistence.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;
import sns.pinocchio.domain.chat.Chat;

public interface ChatRepository extends MongoRepository<Chat, String> {}
