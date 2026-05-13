package com.trulybare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.trulybare.entity")
@EnableJpaRepositories("com.trulybare.repository")
public class TrulyBareApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrulyBareApplication.class, args);
	}

}
