package sns.pinocchio.infrastructure.persistence.mysql;

import java.util.List;
import sns.pinocchio.domain.member.Member;

public interface SearchMemberRepositoryCustom {

  List<Member> searchUsers(String query, int limit, String cursor);
}
