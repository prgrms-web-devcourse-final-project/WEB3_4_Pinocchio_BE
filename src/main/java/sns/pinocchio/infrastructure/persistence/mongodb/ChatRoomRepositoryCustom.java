package sns.pinocchio.infrastructure.persistence.mongodb;

import java.util.List;
import sns.pinocchio.domain.chat.Chat;
import sns.pinocchio.domain.chatroom.ChatRoom;
import sns.pinocchio.domain.chatroom.ChatRoomSortType;

public interface ChatRoomRepositoryCustom {

  List<ChatRoom> findChatRoomsByUserWithCursor(
      String userTsid, String cursor, int limit, ChatRoomSortType sortType);

  List<Chat> findChatsByChatRoomWithCursor(
      String chatRoomTsid, String cursor, int limit, ChatRoomSortType sortType);
}
