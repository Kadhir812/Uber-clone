package com.uber.matching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class MatchingApplication {

	public static void main(String[] args) {
		SpringApplication.run(MatchingApplication.class, args);
	}

}
