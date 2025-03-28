package com.b101.pickTime.config;

import com.b101.pickTime.common.auth.JWTFilter;
import com.b101.pickTime.common.auth.LoginFilter;
import com.b101.pickTime.common.auth.CustomLogoutFilter;
import com.b101.pickTime.common.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final AuthenticationConfiguration authenticationConfiguration;
	private final JWTUtil jwtUtil;
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
	@Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

		http.csrf(csrf -> csrf.disable())
        .formLogin(auth -> auth.disable())
        .httpBasic(auth -> auth.disable())
		.logout(logout -> logout.disable())
        .authorizeHttpRequests(auth -> auth
				.requestMatchers("/login", "/reissue", "/test","/verification/*").permitAll()
				.requestMatchers(HttpMethod.POST, "/user").permitAll()
				.anyRequest().authenticated()
        )

				.addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class)
				.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(new CustomLogoutFilter(jwtUtil), LogoutFilter.class)
				.sessionManagement((session)->session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}
}
