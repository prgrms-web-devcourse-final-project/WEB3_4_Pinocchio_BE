package sns.pinocchio.domain.member;

import com.github.f4b6a3.tsid.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member {

    @Id
    private String id;

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

    private String profileImageUrl;

    private Boolean isActive;

    @Builder
    public Member(String password, String email, String name, String nickname, Tsid id) {
        this.password = password;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.id = Tsid.fast().toString();
    }
}