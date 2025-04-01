package sns.pinocchio.application.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sns.pinocchio.config.global.auth.util.TokenProvider;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.presentation.member.exception.MemberErrorCode;
import sns.pinocchio.presentation.member.exception.MemberException;

@RequiredArgsConstructor
@Service
public class AuthService {

  private final PasswordEncoder passwordEncoder;
  private final TokenProvider tokenProvider;

  // 패스워드 검증
  public void validatePassword(String password, Member member) {
    if (!passwordEncoder.matches(password, member.getPassword())) {
      throw new MemberException(MemberErrorCode.INVALID_PASSWORD);
    }
  }

  // 엑세스토큰 생성
  public String generateAndSaveToken(Member member) {
    String accessToken = tokenProvider.generateAccessToken(member);
    return accessToken;
  }
}
