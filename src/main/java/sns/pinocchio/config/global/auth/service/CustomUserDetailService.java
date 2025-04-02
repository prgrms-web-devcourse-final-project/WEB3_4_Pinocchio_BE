package sns.pinocchio.config.global.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.config.global.auth.util.JwtUtil;
import sns.pinocchio.domain.member.Member;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

  private final MemberService memberService;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

    Member member = memberService.findByEmail(email);
    return new CustomUserDetails(member);
  }

  // UserDetails 생성
  public CustomUserDetails loadUserByAccessToken(String accessToken) {
    Long memberId = JwtUtil.getMember(accessToken);
    Member member = memberService.findById(memberId);
    return new CustomUserDetails(member);
  }
}
