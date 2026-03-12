package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;
import jakarta.annotation.PostConstruct;

// Issue #4: @EnableScheduling required for ApplicationSchedulerService @Scheduled methods
@SpringBootApplication
@EnableScheduling
public class DemoApplication {

	public static void main(String[] args) {
		// Set default timezone to Asia/Ho_Chi_Minh to correctly handle Vietnam time
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
		SpringApplication.run(DemoApplication.class, args);
	}

	@PostConstruct
	public void init() {
		// Ensure timezone is set for the application context
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
	}

}
