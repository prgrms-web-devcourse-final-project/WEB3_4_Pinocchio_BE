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
                auth.requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**")
                    .permitAll()
                    .requestMatchers(
                        "/auth/signup",
                        "/auth/login",
                        "/auth/logout",
                        "/posts/search",
                        "/actuator/health")
                    .permitAll()
                    //  정적 리소스 (React 빌드 파일들) 허용
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
