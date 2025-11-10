package com.jorgeandreu.orders;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class OrdersServiceApplicationTests {

	@Container
	static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

	@DynamicPropertySource
	static void mongoProps(DynamicPropertyRegistry r) {
		r.add("spring.data.mongodb.uri", () -> mongo.getConnectionString() + "/ordersdb");
	}


	@Test
	void contextLoads() {}
}
