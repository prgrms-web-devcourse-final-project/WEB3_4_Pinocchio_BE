package sns.pinocchio.domain.fixtures;

import sns.pinocchio.domain.member.Member;

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

  // 회원 객체 생성
  public Member createMember(String email, String password, String nickname, String name) {
    return Member.builder().email(email).password(password).nickname(nickname).name(name).build();
  }
}
