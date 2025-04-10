package sns.pinocchio.presentation.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sns.pinocchio.application.auth.AuthService;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.application.member.memberDto.request.LoginRequestDto;
import sns.pinocchio.application.member.memberDto.request.SignupRequestDto;
import sns.pinocchio.application.member.memberDto.response.SignupResponseDto;
import sns.pinocchio.config.global.auth.util.TokenProvider;
import sns.pinocchio.domain.member.Member;

@Slf4j
@RequestMapping("/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {

  private final MemberService memberService;
  private final AuthService authService;
  private final TokenProvider tokenProvider;

  // 회원가입
  @PostMapping("/signup")
  public ResponseEntity<SignupResponseDto> signup(
      @RequestBody @Valid SignupRequestDto signupRequestDto) {
    // 계정 생성
    Member member = memberService.createMember(signupRequestDto);

    // 응답 DTO 변환
    SignupResponseDto signupResponseDto =
        new SignupResponseDto(
            "success",
            HttpStatus.CREATED.value(),
            "회원가입에 성공했습니다.",
            SignupResponseDto.UserData.of(member));

    return ResponseEntity.status(HttpStatus.CREATED).body(signupResponseDto);
  }

  @PostMapping("/login")
  public ResponseEntity<SignupResponseDto> login(
      @RequestBody @Valid LoginRequestDto loginRequestDto, HttpServletResponse response) {
    // TODO : log 삭제 해야함
    log.info("요청 url : /auth/login 로그인 API 동작");
    Member member = memberService.findByEmail(loginRequestDto.email());
    log.info("요청 url : /auth/login 이메일 존재 여부{}", member.getEmail());
    authService.validatePassword(loginRequestDto.password(), member);
    log.info("패스워드 존재 여부");
    String accessToken = authService.generateAndSaveToken(member);
    log.info("엑세스토큰 생성 완료");
    String refreshToken = tokenProvider.generateRefreshToken();
    log.info("리프레시토큰 생성 완료");

    memberService.saveRefreshToken(refreshToken, member, response);
    log.info("토큰 응답에 저장 완료");
    SignupResponseDto signupResponseDto =
        new SignupResponseDto(
            "success",
            HttpStatus.OK.value(),
            "로그인에 성공했습니다.",
            SignupResponseDto.UserData.of(member));

    return ResponseEntity.status(HttpStatus.OK)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .body(signupResponseDto);
  }

  // 로그아웃
  @PostMapping("/logout")
  public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
    memberService.tokenClear(request, response);
    return ResponseEntity.ok("로그아웃에 성공했습니다.");
  }
}
