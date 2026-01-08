package com.smtech.SM_Caterer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
public class SmCatererApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmCatererApplication.class, args);
	}

}
