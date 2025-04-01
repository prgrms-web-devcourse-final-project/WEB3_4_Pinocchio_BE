package sns.pinocchio.infrastructure.persistence.mongodb;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import sns.pinocchio.domain.chatroom.ChatRoom;
import sns.pinocchio.domain.chatroom.ChatRoomSortType;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryCustomImpl implements ChatRoomRepositoryCustom {

  private final MongoTemplate mongoTemplate;

  @Override
  public List<ChatRoom> findChatRoomsByUserWithCursor(
      String userTsid, String cursor, int limit, ChatRoomSortType sortType) {

    Query query = new Query();

    // 해당 유저가 포함된 채팅방
    query.addCriteria(Criteria.where("participantTsids").is(userTsid));

    // 커서가 있을 경우, 생성 시간 기준으로 이전 것만 가져오기
    if (cursor != null) {
      query.addCriteria(Criteria.where("createdAtTsid").lt(cursor));
    }

    // 정렬 타입에 맞게, 생성 시간 기준으로 정렬
    if (sortType == ChatRoomSortType.LATEST) {
      query.with(Sort.by(Sort.Direction.DESC, "createdAtTsid"));

    } else {
      query.with(Sort.by(Sort.Direction.ASC, "createdAtTsid"));
    }

    // 제한 개수 설정
    query.limit(limit);

    return mongoTemplate.find(query, ChatRoom.class, "chatrooms");
  }
}
