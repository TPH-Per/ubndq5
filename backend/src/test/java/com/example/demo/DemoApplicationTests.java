package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.TimeZone;

@SpringBootTest
class DemoApplicationTests {

	static {
		// Set default timezone to Asia/Ho_Chi_Minh to correctly handle Vietnam time
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
	}

	@Test
	void contextLoads() {
	}

}
