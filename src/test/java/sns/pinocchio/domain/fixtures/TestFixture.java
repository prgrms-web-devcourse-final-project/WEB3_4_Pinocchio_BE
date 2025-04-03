package sns.pinocchio.domain.fixtures;

import sns.pinocchio.domain.member.Member;
import sns.pinocchio.domain.report.ReportedType;

public class TestFixture {

  // 회원 가입을 위한 JSON 데이터 생성
  public static String createSignUpRequestJson(
      String name, String email, String nickname, String password) {
    return """
            {
            "name": "%s",
            "email": "%s",
            "nickname": "%s",
            "password": "%s"
            }
        """
        .formatted(name, email, nickname, password);
  }

  // 로그인 요청 JSON 데이터 생성
  public static String createLoginRequestJson(String email, String password) {
    return """
            {
            "email": "%s",
            "password": "%s"
            }
        """
        .formatted(email, password);
  }

  // 프로필 수정 요청 JSON 데이터 생성
  public static String updateProfileRequestJson(
      String name,
      String nickname,
      String bio,
      String website,
      String profileImageUrl,
      Boolean isActive) {
    return """
            {
            "name": "%s",
            "nickname": "%s",
            "bio": "%s",
            "website": "%s",
            "profileImageUrl": "%s",
            "isActive": "%s"
            }
        """
        .formatted(name, nickname, bio, website, profileImageUrl, isActive);
  }

  // 새로운 비밀번호 JSON 데이터 생성
  public static String createNewPassword(String currentPassword, String newPassword) {
    return """
            {
            "currentPassword": "%s",
            "newPassword": "%s"
            }
        """
        .formatted(currentPassword, newPassword);
  }

  public static String createPassword(String password) {
    return """
          {
          "password": "%s"
          }
        """
        .formatted(password);
  }

  public static String createReportRequestDto(
      String reportedNickname, ReportedType type, String reason) {
    return """
            {
            "reportedNickname": "%s",
            "reportedType": "%s",
            "reason": "%s"
            }
        """
        .formatted(reportedNickname, type, reason);
  }

  // 회원 객체 생성
  public Member createMember(String email, String password, String nickname, String name) {
    return Member.builder().email(email).password(password).nickname(nickname).name(name).build();
  }
}
