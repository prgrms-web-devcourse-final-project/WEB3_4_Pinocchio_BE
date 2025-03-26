package sns.pinocchio.presentation.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.application.member.memberDto.MemberLoginRequestDto;
import sns.pinocchio.application.member.memberDto.MemberRequestDto;

@RequestMapping("/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {

    private final MemberService memberService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody @Valid MemberRequestDto memberRequestDto) {
        // 계정 생성
        memberService.createMember(memberRequestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("회원가입이 완료되었습니다.");
    }


    // 로그인
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid MemberLoginRequestDto memberLoginRequestDto, HttpServletResponse response) {

        String email = memberLoginRequestDto.email();
        String password = memberLoginRequestDto.password();

        // 이메일 검증
        memberService.validateEmail(email);
        // 패스워드 검증
        memberService.validatePassword(password, email);

        // 토콘 생성 및 저장
        memberService.generateToken(email, response);
        memberService.generateAndSaveRefreshToken(email, response);

        return ResponseEntity.ok("로그인 성공");
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        memberService.logout(request, response);
        return ResponseEntity.ok("로그아웃 성공");
    }
}
