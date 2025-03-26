package sns.pinocchio.domain.member;

import lombok.Builder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

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
    public Member(String password, String email, String name, String nickname) {
        this.password = password;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
    }
}
