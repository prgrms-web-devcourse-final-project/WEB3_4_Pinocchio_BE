package sns.pinocchio.infrastructure.persistence.mysql;

import static sns.pinocchio.domain.member.QMember.member;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import sns.pinocchio.domain.member.Member;

@Repository
@RequiredArgsConstructor
public class SearchMemberRepositoryCustomImpl implements SearchMemberRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Member> searchUsers(String query, int limit, String cursor) {

    BooleanBuilder builder = new BooleanBuilder();

    // 검색어 필터
    if (query != null) {
      builder.or(member.name.containsIgnoreCase(query));
      builder.or(member.nickname.containsIgnoreCase(query));
    }

    // 커서 조건 추가
    if (cursor != null) {
      builder.and(member.tsid.gt(cursor));
    }

    return queryFactory
        .selectFrom(member)
        .where(builder)
        .orderBy(member.id.asc())
        .limit(limit)
        .fetch();
  }
}
