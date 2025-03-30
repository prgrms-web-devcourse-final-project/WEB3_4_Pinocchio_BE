package sns.pinocchio.presentation.mail.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum MailErrorCode {
    MAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "500-1", "메일 전송에 실패했습니다."),
    INVALID_EMAIL_ADDRESS(HttpStatus.BAD_REQUEST, "400-1", "잘못된 이메일 주소입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}