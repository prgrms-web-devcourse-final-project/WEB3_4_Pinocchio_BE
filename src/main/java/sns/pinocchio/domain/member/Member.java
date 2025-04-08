package sns.pinocchio.domain.member;

import com.github.f4b6a3.tsid.TsidCreator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

  private Boolean isActive;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Column(nullable = false, unique = true)
  private String tsid;

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

  public void updateProfile(
      String name,
      String nickname,
      String bio,
      String website,
      String profileImageUrl,
      Boolean isActive) {
    if (name != null) this.name = name;
    if (nickname != null) this.nickname = nickname;
    if (bio != null) this.bio = bio;
    if (website != null) this.website = website;
    if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
    if (isActive != null) this.isActive = isActive;
    this.updatedAt = LocalDateTime.now();
  }

  public void updatePassword(String password) {
    this.password = password;
  }
}
