package sns.pinocchio.config.global.auth.util;

import java.security.SecureRandom;

public class PasswordUtil {

  // 임시 비밀번호 생성
  public static String generateTemporaryPassword() {
    String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
    SecureRandom random = new SecureRandom();
    StringBuilder tempPassword = new StringBuilder(8); // 비밀번호 길이 8
    for (int i = 0; i < 8; i++) {
      int index = random.nextInt(characters.length());
      tempPassword.append(characters.charAt(index));
    }
    return tempPassword.toString();
  }
}
