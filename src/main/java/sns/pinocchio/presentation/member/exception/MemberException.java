package sns.pinocchio.presentation.member.exception;

import org.springframework.http.HttpStatus;

public class MemberException extends RuntimeException {

    private final MemberErrorCode memberErrorCode;

    public MemberException(MemberErrorCode memberErrorCode) {
        super(memberErrorCode.getMessage());
        this.memberErrorCode = memberErrorCode;
    }

    public HttpStatus getStatus() {
        return memberErrorCode.getHttpStatus();
    }

    public String getCode() {
        return memberErrorCode.getCode();
    }
}