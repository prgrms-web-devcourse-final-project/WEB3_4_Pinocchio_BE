package sns.pinocchio.application.member.memberDto;

import lombok.Builder;
import sns.pinocchio.domain.member.Member;

// 사용자 기본 정보만 담는 dto
@Builder
public record MemberInfoDto(
        String id,
        String nickname,
        String email
) {

    public static MemberInfoDto of(Member member) {
        return MemberInfoDto.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .build();
    }
}