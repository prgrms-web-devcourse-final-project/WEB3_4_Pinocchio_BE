package sns.pinocchio.config.global.auth.util;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import sns.pinocchio.presentation.mail.exception.MailErrorCode;
import sns.pinocchio.presentation.mail.exception.MailException;

public class EmailUtil {

  @Autowired private static JavaMailSender javaMailSender;

  public static void sendEmail(String toEmail, String temporaryPassword) {
    try {
      MimeMessage mimeMessage = javaMailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

      helper.setFrom("gorhkd232@naver.com");
      helper.setTo(toEmail);
      helper.setSubject("임시 비밀번호 안내");
      String body =
          "귀하의 임시 비밀번호는 " + temporaryPassword + " 입니다.\n" + "로그인 후 반드시 비밀번호를 변경해주시기 바랍니다.";
      helper.setText(body);
      javaMailSender.send(mimeMessage);
    } catch (Exception e) {
      throw new MailException(MailErrorCode.MAIL_SEND_FAILED);
    }
  }
}
