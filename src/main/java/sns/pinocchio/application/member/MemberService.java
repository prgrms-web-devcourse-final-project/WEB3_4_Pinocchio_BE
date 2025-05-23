package sns.pinocchio.application.member;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sns.pinocchio.application.member.memberDto.request.SignupRequestDto;
import sns.pinocchio.application.member.memberDto.request.UpdateRequestDto;
import sns.pinocchio.config.S3img.S3Uploader;
import sns.pinocchio.config.global.auth.service.cookieService.CookieService;
import sns.pinocchio.config.global.auth.util.EmailUtil;
import sns.pinocchio.config.global.auth.util.JwtUtil;
import sns.pinocchio.config.global.auth.util.PasswordUtil;
import sns.pinocchio.config.global.redis.redisService.RedisService;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.member.MemberRepository;
import sns.pinocchio.presentation.member.exception.MemberErrorCode;
import sns.pinocchio.presentation.member.exception.MemberException;

@RequiredArgsConstructor
@Service
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final CookieService cookieService;
  private final RedisService redisService;
  private final JwtUtil jwtUtil;
  private final S3Uploader s3Uploader;

  // 계정 생성
  @Transactional
  public Member createMember(SignupRequestDto signupRequestDto) {
    // 이메일 중복 체크
    checkEmailDuplicate(signupRequestDto.getEmail());

    // 닉네임 중복 체크
    checkNicknameDuplicate(signupRequestDto.getNickname());

    Member member =
        Member.builder()
            .email(signupRequestDto.getEmail())
            .name(signupRequestDto.getName())
            .nickname(signupRequestDto.getNickname())
            .password(passwordEncoder.encode(signupRequestDto.getPassword()))
            .build();

    this.memberRepository.save(member);

    return member;
  }

  // 사용자 프로필 수정
  @Transactional
  public Member updateProfile(UpdateRequestDto updateRequestDto, MultipartFile image, Long userId)
      throws IOException {
    Member member = findById(userId);

    if (!updateRequestDto.nickname().equals(member.getNickname())) {
      checkNicknameDuplicate(updateRequestDto.nickname());
    }

    String nickname = updateRequestDto.nickname().trim();

    if (nickname.length() < 3) {
      throw new MemberException(MemberErrorCode.NICKNAME_TOO_SHORT);
    }

    // 이미지가 있을 경우 업로드
    if (image != null && !image.isEmpty()) {
      String imageUrl = s3Uploader.uploadFile(image, "post-profile/");
      updateRequestDto =
          updateRequestDto.withProfileImageUrl(imageUrl); // 불변 객체라면 builder 패턴 또는 with 메서드 필요
    }
    member.updateProfile(updateRequestDto);
    memberRepository.save(member);

    return member;
  }

  // 임시 비밀번호 발송
  @Transactional
  public void sendTemporaryPassword(String email) {
    // 회원 조회
    Member member = findByEmail(email);

    // 임시 비밀번호 생성 / 패스워드 암호화 / 비밀번호 변경
    String temporaryPassword = PasswordUtil.generateTemporaryPassword();
    member.updatePassword(passwordEncoder.encode(temporaryPassword));

    // 이메일 발송
    EmailUtil.sendEmail(member.getEmail(), temporaryPassword);
  }

  public void checkEmailDuplicate(String email) {
    memberRepository
        .findByEmail(email)
        .ifPresent(
            member -> {
              throw new MemberException(MemberErrorCode.EMAIL_DUPLICATED);
            });
  }

  public void checkNicknameDuplicate(String nickname) {
    memberRepository
        .findByNickname(nickname)
        .ifPresent(
            member -> {
              throw new MemberException(MemberErrorCode.NICKNAME_DUPLICATED);
            });
  }

  // 리프레시 토큰 쿠키, 레디스 저장
  public void saveRefreshToken(String refreshToken, Member member, HttpServletResponse response) {
    cookieService.addRefreshTokenToCookie(
        refreshToken, jwtUtil.getRefreshTokenExpirationTime(), response);
    redisService.save(
        refreshToken, String.valueOf(member.getId()), jwtUtil.getRefreshTokenExpirationTime());
  }

  public void tokenClear(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = cookieService.getRefreshTokenFromCookie(request);

    cookieService.clearTokenFromCookie(response);
    redisService.addBlackList(refreshToken, jwtUtil.getRefreshTokenExpirationTime());
  }

  @Transactional
  public void deleteMember(Long userId) {
    memberRepository.deleteById(userId);
  }

  @Transactional
  public void changePassword(Member member, String password) {
    member.updatePassword(passwordEncoder.encode(password));
    memberRepository.save(member);
  }

  @Transactional(readOnly = true)
  public Member findByEmail(String email) {
    return memberRepository
        .findByEmail(email)
        .orElseThrow(() -> new MemberException(MemberErrorCode.USER_NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public Member findByNickname(String nickname) {
    return memberRepository
        .findByNickname(nickname)
        .orElseThrow(() -> new MemberException(MemberErrorCode.USER_NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public Member findById(Long memberId) {
    return memberRepository
        .findById(memberId)
        .orElseThrow(() -> new MemberException(MemberErrorCode.USER_NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public Member findByTsid(String tsid) {
    return memberRepository
        .findByTsid(tsid)
        .orElseThrow(() -> new MemberException(MemberErrorCode.USER_NOT_FOUND));
  }
}
