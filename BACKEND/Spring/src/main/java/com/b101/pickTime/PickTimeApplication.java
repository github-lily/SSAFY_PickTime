package com.b101.pickTime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.TimeZone;

@SpringBootApplication
public class PickTimeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PickTimeApplication.class, args);
	}

}
