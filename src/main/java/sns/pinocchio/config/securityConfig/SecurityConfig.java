package sns.pinocchio.config.securityConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sns.pinocchio.config.global.auth.jwt.MemberAuthFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Autowired @Lazy private MemberAuthFilter memberAuthFilter;

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화
        .sessionManagement(
            session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 상태 없는 세션 정책 설정
        .httpBasic(AbstractHttpConfigurer::disable) // 기본 HTTP 인증 비활성화
        .formLogin(AbstractHttpConfigurer::disable) // 폼 로그인 비활성화
        .logout(AbstractHttpConfigurer::disable) // 로그아웃 비활성화
        .authorizeHttpRequests(
            auth ->
                auth.
                     requestMatchers(
                  // === [ Swagger & 문서 접근 허용 ] ===
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**")
                    .permitAll()
                  // === [ 인증 불필요 API ] ===
                    .requestMatchers(
                        "/auth/signup",
                        "/auth/login",
                        "/auth/logout",
                        "/posts/search",
                        "/actuator/health",
                        "/user/password/reset")
                    .permitAll()
   /*<Spring Security의 requestMatchers는 위에서 아래로 순차적으로 평가되므로, 특정 경로에 대한 예외 허용(permitAll)은 가장 위에 위치>*/
                 // === [ 정적 리소스 (React 빌드 파일들)  접근 허용 ] ===
                    .requestMatchers(
                        "/",
                        "/index.html",
                        "/static/**",
                        "/favicon.ico",
                        "/asset-manifest.json",
                        "/manifest.json",
                        "/logo192.png",
                        "/logo512.png")
                    .permitAll()
                        // === [ 인증 필요한 API ] ===
                    .requestMatchers("/user/**").authenticated()
                    .requestMatchers("/posts/**").authenticated()
                    .requestMatchers("/posts/like/**").authenticated()
                    .requestMatchers("/comments/**").authenticated()
                    .requestMatchers("/block/**").authenticated()
                    .requestMatchers("/chat/**").authenticated()
                    .requestMatchers("/search").authenticated()
                    .requestMatchers("/notifications/settings").authenticated()
                  // === [ 모든 나머지 요청 인증 ] ===
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(memberAuthFilter, UsernamePasswordAuthenticationFilter.class);
    ;
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
