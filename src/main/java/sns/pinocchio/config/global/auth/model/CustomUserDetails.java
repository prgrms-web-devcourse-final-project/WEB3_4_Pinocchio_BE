package sns.pinocchio.config.global.auth.model;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import sns.pinocchio.application.member.memberDto.MemberInfoDto;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    // 로그인한 사용자의 정보를 담는 DTO
    private final MemberInfoDto memberInfoDto;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자의 권한 설정
        return List.of(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getPassword() {
        return null; // 비밀번호 반환 - 보안상 null 로 설정
    }

    @Override
    public String getUsername() {
        return memberInfoDto.nickname(); // 닉네임 반환
    }

    public Long getUserId() {
        return memberInfoDto.id(); // 유저 ID 반환
    }

    public String getEmail() {
        return memberInfoDto.email(); // 이메일 반환
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired(); // 계정 만료 여부 설정
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked(); // 계정 잠김 여부 설정
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired(); // 자격 증명(비밀번호) 만료 여부 설정
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled(); // 계정 활성화 여부 설정
    }
}
