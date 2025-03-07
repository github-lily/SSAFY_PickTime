package com.b101.pickTime.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
		
		http.csrf(csrf -> csrf.disable())
        .formLogin(auth -> auth.disable())
        .httpBasic(auth -> auth.disable())
        .authorizeHttpRequests(auth -> auth
                       			.requestMatchers("/**").permitAll()									// 임시로 전부 허용

        );

		return http.build();

		
	}
}
