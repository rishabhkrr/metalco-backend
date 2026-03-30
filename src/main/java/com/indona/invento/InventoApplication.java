package com.indona.invento;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.indona.invento")
public class InventoApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoApplication.class, args);
	}
	
	@PostConstruct
	void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
