package com.ssafy.gumid101.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ssafy.gumid101.OAuth.CustomOAuth2UserService;
import com.ssafy.gumid101.OAuth.OAuth2SuccessHandler;
import com.ssafy.gumid101.jwt.JwtAuthFilter;
import com.ssafy.gumid101.user.UserRepository;

import lombok.RequiredArgsConstructor;

@EnableWebSecurity(debug = true)
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
///oauth2/authorization/google
	private final UserRepository userRepo;
    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler successHandler;
	// 패스워드 암호화에 사용
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		
		return  authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager)
			throws Exception {
		http.cors().disable();// cors 문제 무시
		http.httpBasic().disable(); // 헤더에 username,password 로그인 사용 불가
		http.csrf().disable(); // csrf 보안 사용 안함
		http.anonymous().disable(); // 익명 사용자 허용 x
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // 스프링 시큐리티 컨텍스트 홀더 세션 사용 안함

		//oath 사용한다고 설정, 
		http.oauth2Login().loginPage("/token/expired").successHandler(successHandler)
        .userInfoEndpoint().userService(oAuth2UserService);
		
		
		http.addFilterBefore(new JwtAuthFilter(userRepo),
                UsernamePasswordAuthenticationFilter.class);

		http.authorizeHttpRequests((authz) -> {
			authz.antMatchers("/index/**").authenticated();
			authz.antMatchers("/swagger-ui/**").permitAll();
		});

		// test 과정이기에 전체 허용
		return http.build();
	}


}
