package com.pawvent.pawventserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.pawvent.pawventserver.domain")
@EnableJpaRepositories(basePackages = "com.pawvent.pawventserver.repository")
public class PawventApplication {

	public static void main(String[] args) {
		SpringApplication.run(PawventApplication.class, args);
	}

}
