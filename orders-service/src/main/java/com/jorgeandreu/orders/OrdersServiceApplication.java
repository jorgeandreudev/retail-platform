package com.jorgeandreu.orders;

import org.bson.types.Decimal128;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.math.BigDecimal;
import java.util.List;

@SpringBootApplication(scanBasePackages = "com.jorgeandreu")
@EnableFeignClients(basePackages = "com.jorgeandreu.orders.infrastructure.products")
@EntityScan(basePackages = "com.jorgeandreu.orders.infrastructure.db")
public class OrdersServiceApplication {

	private static final Logger log = LoggerFactory.getLogger(OrdersServiceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(OrdersServiceApplication.class, args);
	}

	@Bean
	public MongoCustomConversions mongoCustomConversions() {
		log.info("=== REGISTERING MONGO CUSTOM CONVERSIONS ===");
		return new MongoCustomConversions(List.of(
				new BigDecimalToDecimal128(),
				new Decimal128ToBigDecimal()
		));
	}

	@WritingConverter
	static class BigDecimalToDecimal128 implements Converter<BigDecimal, Decimal128> {
		@Override
		public org.bson.types.Decimal128 convert(BigDecimal source) {
			log.info("=== WRITING CONVERTER CALLED - Converting BigDecimal: {} to Decimal128", source);
			return source == null ? null : new org.bson.types.Decimal128(source);
		}
	}

	@ReadingConverter
	static class Decimal128ToBigDecimal implements Converter<org.bson.types.Decimal128, BigDecimal> {
		@Override
		public BigDecimal convert(org.bson.types.Decimal128 source) {
			log.info("=== READING CONVERTER CALLED - Converting Decimal128: {} to BigDecimal", source);
			return source == null ? null : source.bigDecimalValue();
		}
	}

}
