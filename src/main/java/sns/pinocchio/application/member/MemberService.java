package sns.pinocchio.application.member;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sns.pinocchio.application.member.memberDto.request.SignupRequestDto;
import sns.pinocchio.application.member.memberDto.request.UpdateRequestDto;
import sns.pinocchio.config.global.auth.service.cookieService.CookieService;
import sns.pinocchio.config.global.auth.util.EmailUtil;
import sns.pinocchio.config.global.auth.util.JwtUtil;
import sns.pinocchio.config.global.auth.util.PasswordUtil;
import sns.pinocchio.infrastructure.redis.redisService.RedisService;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.member.MemberRepository;
import sns.pinocchio.presentation.auth.exception.AuthErrorCode;
import sns.pinocchio.presentation.auth.exception.AuthException;
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

  @Transactional
  public Member createMember(SignupRequestDto signupRequestDto) {
    checkEmailDuplicate(signupRequestDto.getEmail());
    checkNicknameDuplicate(signupRequestDto.getNickname());

    Member member =
        Member.builder()
            .email(signupRequestDto.getEmail())
            .name(signupRequestDto.getName())
            .nickname(signupRequestDto.getNickname())
            .password(passwordEncoder.encode(signupRequestDto.getPassword()))
            .build();
    memberRepository.save(member);

    return member;
  }

  @Transactional
  public Member updateProfile(Long memberId, UpdateRequestDto updateRequestDto) {
    Member member = findById(memberId);
    checkNicknameDuplicate(updateRequestDto.nickname());

    member.updateProfile(
        updateRequestDto.name(),
        updateRequestDto.nickname(),
        updateRequestDto.bio(),
        updateRequestDto.website(),
        updateRequestDto.profileImageUrl(),
        updateRequestDto.isActive());

    return member;
  }

  @Transactional
  public void sendTemporaryPassword(String email) {
    Member member = findByEmail(email);

    String temporaryPassword = PasswordUtil.generateTemporaryPassword();
    member.updatePassword(passwordEncoder.encode(temporaryPassword));

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

  public void saveRefreshToken(String refreshToken, Member member, HttpServletResponse response) {
    cookieService.addRefreshTokenToCookie(
        refreshToken, jwtUtil.getRefreshTokenExpirationTime(), response);
    redisService.save(
        refreshToken, String.valueOf(member.getId()), jwtUtil.getRefreshTokenExpirationTime());
  }

  public void tokenClear(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = cookieService.getRefreshTokenFromCookie(request);

    if (refreshToken == null) {
      throw new AuthException(AuthErrorCode.INVALID_TOKEN);
    }
    cookieService.clearTokenFromCookie(response);
    redisService.addBlackList(refreshToken, jwtUtil.getRefreshTokenExpirationTime());
  }


  @Transactional
  public void deleteMember(Member member) {
    memberRepository.deleteById(member.getId());
  }

  @Transactional
  public void changePassword(Member member, String password) {
    member.updatePassword(passwordEncoder.encode(password));
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
