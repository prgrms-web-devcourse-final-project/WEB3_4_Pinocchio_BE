package sns.pinocchio.application.social.kakao;

import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {
    private final String email;
    private final String nickname;

    public OAuthAttributes(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
    }

    public static OAuthAttributes of(String registrationId, Map<String, Object> attributes) {
        if ("kakao".equals(registrationId)) {
            return ofKakao(attributes);
        }
        throw new IllegalArgumentException("Unsupported provider: " + registrationId);
    }

    private static OAuthAttributes ofKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String email = (String) kakaoAccount.get("email");
        String nickname = (String) profile.get("nickname");

        return new OAuthAttributes(email, nickname);
    }
}