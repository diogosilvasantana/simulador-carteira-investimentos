package br.com.simuladorcarteira.identity.adapters.in.web;

import br.com.simuladorcarteira.identity.domain.LoginThrottle;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Tudo fechado por padrão (ADR-014): só o login, a saúde para o Coolify e a ingestão da ponte (que
 * tem token próprio) ficam abertos. Sessão no servidor em cookie HttpOnly; o navegador nunca vê
 * uma credencial que possa vazar por JavaScript.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties.class)
class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService users(SecurityProperties properties) {
    return new InMemoryUserDetailsManager(
        User.withUsername(properties.username())
            .password(properties.passwordHash())
            .roles("OWNER")
            .build());
  }

  @Bean
  LoginThrottle loginThrottle(SecurityProperties properties) {
    return new LoginThrottle(
        properties.maxFailuresPerClient(), properties.maxFailuresOverall(), properties.lockout());
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, LoginThrottle throttle, Clock clock)
      throws Exception {
    http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        LoginThrottleFilter.LOGIN_PATH, "/actuator/health/**", "/ingest")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        // A ponte não é um navegador: autentica pelo token no handshake, sem cookie nem CSRF.
        .csrf(csrf -> csrf.spa().ignoringRequestMatchers("/ingest"))
        .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
        .addFilterBefore(
            new LoginThrottleFilter(throttle, clock), UsernamePasswordAuthenticationFilter.class)
        .formLogin(
            form ->
                // A tela de login é do Angular; isso só desliga a página padrão do Spring.
                form.loginPage("/login")
                    .loginProcessingUrl(LoginThrottleFilter.LOGIN_PATH)
                    .successHandler(
                        (request, response, authentication) -> {
                          throttle.recordSuccess(request.getRemoteAddr());
                          response.setStatus(HttpStatus.NO_CONTENT.value());
                        })
                    .failureHandler(
                        (request, response, exception) -> {
                          throttle.recordFailure(request.getRemoteAddr(), clock.instant());
                          response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        }))
        .logout(
            logout ->
                logout
                    .logoutUrl("/api/auth/logout")
                    .deleteCookies("JSESSIONID")
                    .logoutSuccessHandler(
                        new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))
        // Sem sessão, 401 sem redirecionar: quem leva à tela de login é o Angular.
        .exceptionHandling(
            errors ->
                errors.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .headers(
            headers ->
                headers
                    .frameOptions(frame -> frame.deny())
                    .referrerPolicy(
                        referrer ->
                            referrer.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)));
    return http.build();
  }
}
