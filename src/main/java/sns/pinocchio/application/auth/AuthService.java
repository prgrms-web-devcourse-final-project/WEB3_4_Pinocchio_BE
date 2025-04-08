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

  public void validatePassword(String currentPassword, Member member) {
    if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
      throw new MemberException(MemberErrorCode.INVALID_PASSWORD);
    }
  }

  public void validateSamePassword(String currentPassword, String newPassword) {
    if (currentPassword.equals(newPassword)) {
      throw new MemberException(MemberErrorCode.INVALID_PASSWORD);
    }
  }

  public String generateAndSaveToken(Member member) {
    String accessToken = tokenProvider.generateAccessToken(member);
    return accessToken;
  }
}
