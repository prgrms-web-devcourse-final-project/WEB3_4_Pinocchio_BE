package sns.pinocchio.presentation.member.exception;

import org.springframework.http.HttpStatus;

// member 관련 예외 처리
public class MemberException extends RuntimeException {

  private final MemberErrorCode memberErrorCode;

  public MemberException(MemberErrorCode memberErrorCode) {
    super(memberErrorCode.getMessage());
    this.memberErrorCode = memberErrorCode;
  }

  public MemberErrorCode getMemberErrorCode() {
    return memberErrorCode;
  }

  public HttpStatus getStatus() {
    return memberErrorCode.getHttpStatus();
  }

  public String getCode() {
    return memberErrorCode.getCode();
  }
}
