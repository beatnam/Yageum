package com.yageum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final MyUserDetailsService myUserDetailsService;

    // 비밀번호 암호화 설정
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()
                .requestMatchers("/member/**").permitAll()
                .requestMatchers("/cashbook/**","/quest/**").permitAll()
                .requestMatchers("/mypage/mdelete").permitAll()
                .requestMatchers("/mypage/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/callback/**","/userInfo/**").permitAll()
                .requestMatchers("/openbanking/**").permitAll()
                .requestMatchers("/consumption/**", "/eanalysis/**", "/efeedback/**", "/canalysis/**", "/bplanner/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/").permitAll()
                .requestMatchers("/admin/**","/user_detail/**","/authority/**").hasRole("ADMIN")
                .requestMatchers("/notice/**").hasAnyRole("USER","ADMIN")
                .anyRequest().authenticated()
            )
            .securityContext(security -> security
                .securityContextRepository(new HttpSessionSecurityContextRepository())
            )
                    .sessionManagement(session -> session
                        .invalidSessionUrl("/member/login?expired") // 세션 만료 시 이동할 URL
                        .maximumSessions(1)                         // 동시에 로그인 가능한 세션 수 제한
                        .maxSessionsPreventsLogin(false)            // 기존 세션 만료 후 새 세션 허용
                        .expiredUrl("/member/login?duplicate")      // 다른 곳에서 로그인 시 처리 URL
                    )
                    .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/")
                    )
                    .userDetailsService(myUserDetailsService)
                    .build();
            }
//            .formLogin(form -> form
//                .loginPage("/member/login")
//                .loginProcessingUrl("/member/loginPro")
//                .usernameParameter("memberId")
//                .passwordParameter("memberPasswd")
//                .defaultSuccessUrl("/cashbook/main", true)
//                .failureUrl("/member/login")
//                .permitAll()
//            )
   
    }
    
    
