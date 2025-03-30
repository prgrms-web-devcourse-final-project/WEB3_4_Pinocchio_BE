package sns.pinocchio.infrastructure.persistence.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import sns.pinocchio.domain.chatroom.ChatRoom;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

  @Query("{ 'participantTsids': { $all: ?0 } }")
  Optional<ChatRoom> findByParticipantTsids(List<String> participantTsids);
}
