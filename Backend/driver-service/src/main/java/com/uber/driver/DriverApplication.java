package com.uber.driver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class DriverApplication {

	public static void main(String[] args) {
		SpringApplication.run(DriverApplication.class, args);
	}

}
