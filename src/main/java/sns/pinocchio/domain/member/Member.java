package sns.pinocchio.domain.member;

import com.github.f4b6a3.tsid.TsidCreator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sns.pinocchio.application.member.memberDto.UpdateRequestDto;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String nickname;

    private String bio;

    private String website;

    @Column(nullable = false)
    private String password;

    @Column(columnDefinition = "TEXT")
    private String profileImageUrl;

    private Boolean isActive; // 기본값을 FALSE 로 설정

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false, unique = true)
    private String tsid;

    // 회원가입
    @Builder
    public Member(String email, String name, String nickname, String password) {
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.password = password;
        this.isActive = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.tsid = TsidCreator.getTsid().toString();
    }

    // 프로필 수정
    public void updateProfile(UpdateRequestDto dto) {
        if (dto.name() != null) this.name = dto.name();
        if (dto.nickname() != null) this.nickname = dto.nickname();
        if (dto.bio() != null) this.bio = dto.bio();
        if (dto.website() != null) this.website = dto.website();
        if (dto.profileImageUrl() != null) this.profileImageUrl = dto.profileImageUrl();
        if (dto.isActive() != null) this.isActive = dto.isActive();
        this.updatedAt = LocalDateTime.now();
    }

    // 비밀번호 변경
    public void updatePassword(String password) {
        this.password = password;
    }
}