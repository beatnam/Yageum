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
                .requestMatchers("/mypage/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/consumption/**", "/eanalysis/**", "/efeedback/**", "/canalysis/**", "/bplanner/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/").permitAll()
                .requestMatchers("/admin/**","/user_detail/**","/authority/**").hasRole("ADMIN")
                .requestMatchers("/notice/**").hasAnyRole("USER","ADMIN")
                .anyRequest().authenticated()
            )
            .securityContext(security -> security
                .securityContextRepository(new HttpSessionSecurityContextRepository())
            )
//            .formLogin(form -> form
//                .loginPage("/member/login")
//                .loginProcessingUrl("/member/loginPro")
//                .usernameParameter("memberId")
//                .passwordParameter("memberPasswd")
//                .defaultSuccessUrl("/cashbook/main", true)
//                .failureUrl("/member/login")
//                .permitAll()
//            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/")
            )
            .userDetailsService(myUserDetailsService)
            .build();
    }
}