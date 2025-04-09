package sns.pinocchio.config.global.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.config.global.auth.model.CustomUserPrincipal;
import sns.pinocchio.config.global.auth.util.TokenProvider;
import sns.pinocchio.domain.member.Member;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final TokenProvider tokenProvider;
  private final MemberService memberService;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
    Member member = memberService.findById(userPrincipal.getMember().getId());

    String accessToken = tokenProvider.generateAccessToken(member);
    String refreshToken = tokenProvider.generateRefreshToken();

    memberService.saveRefreshToken(refreshToken, member, response);

    response.setHeader("Authorization", "Bearer " + accessToken);

    // accessToken은 프론트로 리다이렉트하면서 전달 (쿼리스트링)
    String redirectUri = "https://frontend.site.com/oauth-success?token=" + accessToken;

    response.sendRedirect(redirectUri); }
}
