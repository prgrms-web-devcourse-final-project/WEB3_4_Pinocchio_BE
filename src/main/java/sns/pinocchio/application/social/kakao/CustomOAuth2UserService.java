package sns.pinocchio.application.social.kakao;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import sns.pinocchio.config.global.auth.model.CustomUserPrincipal;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.member.MemberRepository;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 기본적으로 사용자 정보 가져오기
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 어떤 서비스에서 로그인했는지 ex) kakao, google 등
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 사용자 정보
        Map<String, Object> attributes = oAuth2User.getAttributes();
        OAuthAttributes oAuthAttributes = OAuthAttributes.of(registrationId, attributes);

        // 회원 정보 조회 또는 등록
        Member member = saveOrUpdate(oAuthAttributes);

        return new CustomUserPrincipal(member, attributes);
    }

    private Member saveOrUpdate(OAuthAttributes attributes) {
        return memberRepository.findByEmail(attributes.getEmail())
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                            .email(attributes.getEmail())
                            .name(attributes.getNickname())
                            .nickname(attributes.getNickname())
                            .build();
                    return memberRepository.save(newMember);
                });
    }
}