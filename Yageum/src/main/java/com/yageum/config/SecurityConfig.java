package com.yageum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final MyUserDetailsService myUserDetailsService;

	// 암호화 설정
	@Bean
	public PasswordEncoder passwordEncoder() {
//  	BCryptPasswordEncoder : 스프링 시큐리티에서 제공하는 클래스 중 하나
//						 	  : 비밀번호를 암호화하는데 사용할 수 있는 메서드를 가진 클래스
//		BCrypt 해싱 함수를 사용해서 비밀번호를 인코딩해주는 메서드 제공
//		저장소에 저장된 비밀번호의 일치 여부를 확인해주는 메서드 제공
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		// 어떤 주소에 어떤 권한을 가진 사람이 접근할 수 있는지 설정
		return http
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/css/**","/js/**","/img/**").permitAll()
						.requestMatchers("/member/**").permitAll()
						.requestMatchers("/cashbook/**").permitAll()
						.requestMatchers("/").permitAll()
						.requestMatchers("/consumption/**", "/eanalysis/**", "/efeedback/**", "/canalysis/**", "/bplanner/**").hasAnyRole("USER", "ADMIN")
						.requestMatchers("/list/**").hasRole("ADMIN").anyRequest()
						.authenticated())
				.formLogin(
						form -> form.loginPage("/login").loginProcessingUrl("/loginPro").usernameParameter("memberId")
								.passwordParameter("pass").defaultSuccessUrl("/cashbook/main").failureUrl("/login"))
				.logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
						.logoutSuccessUrl("/"))
				.userDetailsService(myUserDetailsService).build();
	}

}
