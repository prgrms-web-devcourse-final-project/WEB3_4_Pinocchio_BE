package sns.pinocchio.config.global.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.application.member.memberDto.MemberInfoDto;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.config.global.auth.util.JwtUtil;
import sns.pinocchio.domain.member.Member;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Member member = memberService.findByUsername(username);
        return new CustomUserDetails(MemberInfoDto.of(member));
    }

    // UserDetails 생성
    public CustomUserDetails loadUserByAccessToken(String accessToken) {
        return new CustomUserDetails(jwtUtil.getMemberInfoDto(accessToken));
    }
}