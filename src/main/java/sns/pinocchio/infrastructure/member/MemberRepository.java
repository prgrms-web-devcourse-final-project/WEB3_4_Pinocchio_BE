package sns.pinocchio.infrastructure.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.persistence.mysql.SearchMemberRepositoryCustom;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, SearchMemberRepositoryCustom {
  Optional<Member> findByEmail(String email);
  Optional<Member> findByNickname(String nickname);
  Optional<Member> findByName(String name);
  Optional<Member> findByTsid(String tsid);
}