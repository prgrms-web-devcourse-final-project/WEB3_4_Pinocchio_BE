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

  // 현재 비밀번호가 맞는지 확인
  public void validatePassword(String currentPassword, Member member) {
    if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
      throw new MemberException(MemberErrorCode.INVALID_PASSWORD);
    }
  }

  // 새 비밀번호가 이전 비밀번호와 같은지 확인
  public void validateSamePassword(String currentPassword, String newPassword) {
    if (currentPassword.equals(newPassword)) {
      throw new MemberException(MemberErrorCode.INVALID_PASSWORD);
    }
  }

  // 엑세스토큰 생성
  public String generateAndSaveToken(Member member) {
    String accessToken = tokenProvider.generateAccessToken(member);
    return accessToken;
  }
}
