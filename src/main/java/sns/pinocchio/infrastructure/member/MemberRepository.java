package sns.pinocchio.infrastructure.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sns.pinocchio.domain.member.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
}
