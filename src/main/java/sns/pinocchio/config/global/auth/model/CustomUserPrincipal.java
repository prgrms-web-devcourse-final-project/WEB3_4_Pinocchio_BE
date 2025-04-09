package sns.pinocchio.config.global.auth.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import sns.pinocchio.domain.member.Member;

@RequiredArgsConstructor
public class CustomUserPrincipal implements UserDetails, OAuth2User {

  private final Member member;
  private Map<String, Object> attributes; // OAuth2 로그인 시 사용자 정보

  // 일반 로그인용 생성자
  public CustomUserPrincipal(Member member) {
    this.member = member;
  }

  // OAuth2 로그인용 생성자
  public CustomUserPrincipal(Member member, Map<String, Object> attributes) {
    this.member = member;
    this.attributes = attributes;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().getKey()));
  }

  @Override
  public String getPassword() {
    return member.getPassword(); // 일반 로그인을 위한 비밀번호 (소셜 로그인은 null)
  }

  @Override
  public String getUsername() {
    return member.getName(); // 사용자 이름 또는 이메일
  }

  public Long getUserId() {
    return member.getId();
  }

  public String getUserTsid() {
    return member.getTsid();
  }

  public String getEmail() {
    return member.getEmail();
  }

  public Member getMember() {
    return member;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  // 아래는 OAuth2User 관련 메서드들

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public String getName() {
    return member.getName(); // OAuth2에서 사용자 식별자 (ID 역할)
  }
}
