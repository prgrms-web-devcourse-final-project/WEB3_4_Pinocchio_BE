package sns.pinocchio.infrastructure.persistence.mongodb;

import java.time.Instant;
import java.util.List;
import sns.pinocchio.domain.chatroom.ChatRoom;
import sns.pinocchio.domain.chatroom.ChatRoomSortType;

public interface ChatRoomRepositoryCustom {

  List<ChatRoom> findChatRoomsByUserWithCursor(
      String userTsid, Instant cursor, int limit, ChatRoomSortType sortType);
}
