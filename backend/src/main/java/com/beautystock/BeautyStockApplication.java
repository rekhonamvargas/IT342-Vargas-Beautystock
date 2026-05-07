package com.beautystock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BeautyStockApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeautyStockApplication.class, args);
	}

}
