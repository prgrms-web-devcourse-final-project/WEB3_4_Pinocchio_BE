package sns.pinocchio.infrastructure.persistence.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;
import sns.pinocchio.domain.chatroom.ChatRoom;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

  Optional<ChatRoom> findByParticipantsTsidContainingAll(List<String> senderId);
}
