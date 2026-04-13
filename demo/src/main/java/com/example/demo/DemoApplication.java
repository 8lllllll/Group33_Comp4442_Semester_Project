package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	// Initialize users
	@Bean
	public org.springframework.boot.CommandLineRunner initUsers(UserRepository userRepository) {
		return args -> {
			if (userRepository.count() == 0) {
				User admin = new User();
				admin.setUsername("admin");
				admin.setPassword("admin123");
				admin.setRole("ADMIN");

				User normalUser = new User();
				normalUser.setUsername("tom");
				normalUser.setPassword("tom123");
				normalUser.setRole("USER");

				userRepository.save(admin);
				userRepository.save(normalUser);
			}
		};
	}

}
