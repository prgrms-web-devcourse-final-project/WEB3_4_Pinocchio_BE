package sns.pinocchio.presentation.member.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum MemberErrorCode {
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "존재하지 않는 사용자입니다."),
  INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "USER_401", "비밀번호가 올바르지 않습니다."),
  ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN, "USER_403", "정지된 계정입니다."),
  UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "USER_401", "로그인이 필요합니다."),
  EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "이메일이 존재하지 않습니다."),
  EMAIL_DUPLICATED(HttpStatus.BAD_REQUEST, "USER_409", "이미 사용 중인 이메일입니다."),
  NICKNAME_DUPLICATED(HttpStatus.BAD_REQUEST, "USER_409", "이미 사용 중인 닉네임입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
