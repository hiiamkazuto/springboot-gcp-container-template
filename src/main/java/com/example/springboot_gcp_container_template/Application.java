package com.example.springboot_gcp_container_template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
@RestController
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@GetMapping("/")
	public HelloResponse hello() {
		log.info("Hello, Log!");
		return new HelloResponse("Hello, Spring Boot!");
	}

	public record HelloResponse(String message) {
	}

}
