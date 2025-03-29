package sns.pinocchio.config.global.auth.exception.email;

import org.springframework.http.HttpStatus;

public class MailException extends RuntimeException {
    private final MailErrorCode mailErrorCode;

    public MailException(MailErrorCode mailErrorCode) {
        super(mailErrorCode.getMessage());
        this.mailErrorCode = mailErrorCode;
    }

    public HttpStatus getStatus() {
        return mailErrorCode.getHttpStatus();
    }

    public String getCode() {
        return mailErrorCode.getCode();
    }
}