package com.jorgeandreu.products;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.jorgeandreu")
@EnableJpaRepositories(basePackages = "com.jorgeandreu.products.infrastructure.db")
@EntityScan(basePackages = "com.jorgeandreu.products.infrastructure.db")
public class ProductsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductsServiceApplication.class, args);
	}

}
